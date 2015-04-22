// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2010-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.cloudcoder.builder2.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.cloudcoder.builder2.model.ProcessStatus;
import org.cloudcoder.builder2.model.WrapperMode;
import org.cloudcoder.builder2.util.ProcessUtil;
import org.cloudcoder.builder2.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Run a subprocess, capturing its stdout and stderr as text.
 * Optionally, send text to the stdin of the process.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class ProcessRunner {
	private static final Logger logger=LoggerFactory.getLogger(ProcessRunner.class);
    
	private Properties config;
	private WrapperMode wrapperMode;
	
	private String statusMessage = "";
	
	private boolean processStarted;
	private int exitCode;
	private ProcessStatus status;
	
	private volatile Process process;
	private Thread exitValueMonitor;
	private String stdin;
	private IOutputCollector stdoutCollector;
	private IOutputCollector stderrCollector;
	private InputSender stdinSender;
	
	private Map<String,String> env;
	
	/**
	 * Constructor.
	 * 
	 * @param config builder configuration properties
	 */
	public ProcessRunner(Properties config) {
		this.config = config;
		this.wrapperMode = WrapperMode.SCRIPT;
		env = new HashMap<String, String>(System.getenv());
	    status = ProcessStatus.UNKNOWN;
	}
	
	/**
	 * Get the map that stores the environment variables to be used
	 * when the process is run.  This map can be modified in order
	 * to add, remove, or redefine environment variables.
	 * 
	 * @return environment variable map
	 */
	public Map<String, String> getEnv() {
		return env;
	}
	
	/**
	 * Get the builder configuration properties.
	 * 
	 * @return the builder configuration properties.
	 */
	public Properties getConfig() {
		return config;
	}
	
	/**
	 * Set the {@link WrapperMode}.
	 * 
	 * @param wrapperMode the {@link WrapperMode}
	 */
	public void setWrapperMode(WrapperMode wrapperMode) {
		this.wrapperMode = wrapperMode;
	}
	
	/**
	 * Set text to send to the process as its standard input.
	 * 
	 * @param stdin text to send to the process as its standard input
	 */
	public void setStdin(String stdin) {
		this.stdin = stdin;
	}
	
	/**
	 * Create the environment array defining the environment
	 * variables for the process.  The environment will
	 * contain all of the environment variables in the parent process,
	 * plus any extra ones specified by the extraVars parameter.
	 * 
	 * @param extraVars extra environment variables to define for the process,
	 *                  in the form VAR=value
	 * @return enviroment array
	 */
	protected String[] getEnvp(String... extraVars) {
		String[] curEnvp = ProcessUtil.getEnvArray(env);
	    String[] envp = new String[curEnvp.length + extraVars.length];
	    System.arraycopy(curEnvp, 0, envp, 0, curEnvp.length);
	    System.arraycopy(extraVars, 0, envp, curEnvp.length, extraVars.length);
	    return envp;
	}
	
	public String getStatusMessage() {
		return statusMessage;
	}
	
	public boolean runSynchronous(File workingDir, String... command) {
		// wrap command (by default, using the runProcess.sh script)
		command = wrapCommand(command);
		
		// exec command
		logger.info("Running in {} the command: {}", workingDir.toString(), StringUtil.mergeOneLine(command));
		try {
			// Create a temp file in which the runProcess.sh script can save
			// the exit status of the process.
			File exitStatusFile = File.createTempFile("ccxs", ".txt", workingDir);
			//logger.debug("Creating exit status file " + exitStatusFile.getPath());
			exitStatusFile.deleteOnExit();

			// Start process, setting CC_PROC_STAT_FILE env var
			// to indicate where runProcess.sh should write the process's
			// exit status information
			process = Runtime.getRuntime().exec(
					command,
					getEnvp("CC_PROC_STAT_FILE=" + exitStatusFile.getPath()),
					workingDir);

			// Collect process output
			stdoutCollector = createOutputCollector(process.getInputStream());
			stderrCollector = createOutputCollector(process.getErrorStream());
			stdoutCollector.start();
			stderrCollector.start();

			// If stdin was provided, send it
			if (stdin != null) {
				//System.out.println("Creating InputSender for input: " + stdin);
				stdinSender = new InputSender(process.getOutputStream(), stdin);
				stdinSender.start();
			}

			// wait for process and output collector threads to finish
			exitCode = process.waitFor();
			stdoutCollector.join();
			stderrCollector.join();
			if (stdinSender != null) {
				stdinSender.join();
			}
			
			// Read the process's exit status information
			readProcessExitStatus(exitStatusFile);
			return true;
		} catch (IOException e) {
			statusMessage = "Could not execute process: " + e.getMessage();
		} catch (InterruptedException e) {
			statusMessage = "Process was interrupted (infinite loop killed?)";
		}
		return false;
	}

	private String[] wrapCommand(String[] command) {
		List<String> cmd = new ArrayList<String>();
		
		switch (wrapperMode) {
		case NATIVE_EXE:
			RunProcessNativeExe runProc = RunProcessNativeExe.getInstance(config);
			if (runProc.getNativeExePath() != null) {
				// Native exe process wrapper exists, so use it.
				cmd.add(runProc.getNativeExePath());
				break;
			}
			
			// There was a problem compiling the native exe wrapper.
			// Fall through!
			
		case SCRIPT:
			cmd.add("/bin/bash");
			cmd.add(RunProcessScript.getInstance(config));
			break;
		}
		
		cmd.addAll(Arrays.asList(command));
		return cmd.toArray(new String[cmd.size()]);
	}

	/**
	 * Create an IOutputCollector to be used to collect the stdout/stderr
	 * of the process.  Subclasses may override to precisely control how
	 * output is collected (for example, to limit the number of bytes/lines
	 * that will be collected.)
	 * 
	 * Default implementation returns an {@link OutputCollector}, which
	 * reads an unlimited amount of output.
	 * 
	 * @param inputStream the InputStream for the process's stdout or stderr
	 * @return an IOutputCollector to collect the process's stdout or stderr
	 */
	protected IOutputCollector createOutputCollector(InputStream inputStream) {
		return new OutputCollector(inputStream);
	}
	
	/**
	 * Read the file written by the runProcess.sh script
	 * which contains information about the process's exit status.
	 * 
	 * @param exitStatusFile file containing information about the process's exit status
	 */
	private void readProcessExitStatus(File exitStatusFile) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(exitStatusFile));
			String status = reader.readLine();
			String exitCode = reader.readLine();
			if (status != null && exitCode != null) {
				logger.debug("Read process exit status file: status={}, exitCode={}", status, exitCode);
				
				// Second line of file should be the exit code
				this.exitCode = Integer.parseInt(exitCode);
				
				if (status.equals("failed_to_execute")) {
					// The process could not be started
					this.processStarted = false;
					this.statusMessage = "Process could not be started";
					this.status = ProcessStatus.COULD_NOT_START;
					
					logger.debug("process stderr is {}", StringUtil.mergeOneLine(stderrCollector.getCollectedOutput()));
				} else if (status.equals("exited")) {
					// The process exited normally.
					this.processStarted = true;
					this.statusMessage = "Process exited";
					this.status = ProcessStatus.EXITED;
				} else if (status.equals("terminated_by_signal")) {
					// The process was killed by a signal.
					// The exit code is the signal that terminated the process.
					this.processStarted = true;
					this.statusMessage = "Process crashed (terminated by signal " + this.exitCode + ")";
					this.status = ProcessStatus.KILLED_BY_SIGNAL;
				} else {
					// Should not happen.
					logger.warn("Unknown process exit status " + status);
					this.statusMessage = "Process status could not be determined";
					this.status = ProcessStatus.COULD_NOT_START;
				}
			}
		} catch (IOException e) {
			logger.warn("IOException trying to read process status file");
			this.statusMessage = "Process status could not be determined";
			this.status = ProcessStatus.COULD_NOT_START;
		} catch (NumberFormatException e) {
			logger.warn("NumberFormatException trying to read process status file");
			this.statusMessage = "Process status could not be determined";
			this.status = ProcessStatus.COULD_NOT_START;
		} finally {
			IOUtils.closeQuietly(reader);
			exitStatusFile.delete();
		}
	}

	public void runAsynchronous(final File workingDir, final String... command) {
	    exitValueMonitor=new Thread() {
	        public void run() {
	            runSynchronous(workingDir, command);
	        }
	    };
	    exitValueMonitor.start();
	}
	
	/**
	 * Find out whether or not the exit status of this process is known.
	 * Because in Java it's not directly possible to find out things about
	 * how a process was terminated (such as whether it was killed by
	 * a signal), we use a wrapper script to collect information about the
	 * process's status and write this to a file that this class can read.
	 * However, we can't rule out the possibility that this file wasn't
	 * written or was corrupted in some way.  If this method returns true,
	 * then the process's exit status information is definitely known.
	 * <bImportant:</b> don't call this unless the process is definitely not running.
	 * 
	 * @return true if the process's exit status is definitely known,
	 *         false otherwise
	 */
	public boolean isExitStatusKnown() {
		return status != ProcessStatus.COULD_NOT_START;
	}
	
	/**
	 * Find out whether or not the process was actually started.
	 * <b>Important:</b>: don't call this unless the process is definitely not running.
	 * 
	 * @return the processStarted
	 */
	public boolean isProcessStarted() {
		return processStarted;
	}
	
	/**
	 * Get the {@link ProcessStatus}.
	 * <b>Important:</b>: don't call this unless the process is definitely not running.
	 * 
	 * @return the {@link ProcessStatus}
	 */
	public ProcessStatus getStatus() {
		if (status == ProcessStatus.KILLED_BY_SIGNAL) {
			if (exitCode == 9 || exitCode == 24) {
				// Special case: if the process was killed by signals 9 (KILL) or 24 (XCPU),
				// treat as a timeout.
				return ProcessStatus.TIMED_OUT;
			} else if (exitCode == 25) {
				// Special case: process killed with SIGXFSZ, file size limit exceeded.
				return ProcessStatus.FILE_SIZE_LIMIT_EXCEEDED;
			}
		}
		return status;
	}
	
	/**
	 * Get the terminated process's exit code.
	 * If the process was killed by a signal, then the exit code is
	 * the number of the signal that killed the process.
	 * <b>Important:</b>: don't call this unless the process is definitely not running.
	 * 
	 * @return process's exit code, or the number of the signal that
	 *         killed the process
	 */
	public int getExitCode() {
		return exitCode;
	}

	/**
	 * @return stdout as a single string
	 */
	public String getStdout() {
		return StringUtil.merge(getStdoutAsList());
	}

	/**
	 * @return the standard output written by the process as a List of strings
	 */
	public List<String> getStdoutAsList() {
		return stdoutCollector.getCollectedOutput();
	}

	/**
	 * @return stderr as a single string
	 */
	public String getStderr() {
		return StringUtil.merge(getStderrAsList());
	}

	/**
	 * @return the standard error written by the process as a List of strings
	 */
	public List<String> getStderrAsList() {
		// Special case: if the process was killed because it exceeded
		// a resource limit, its stderr is probably not useful.
		ProcessStatus status = getStatus();
		if (status == ProcessStatus.TIMED_OUT || status == ProcessStatus.FILE_SIZE_LIMIT_EXCEEDED) {
			return Collections.emptyList();
		} else {
			return stderrCollector.getCollectedOutput();
		}
	}

	/**
	 * Check whether or not the process is still running.
	 * 
	 * @return true if the process is still running, false if it has completed
	 */
    public boolean isRunning() {
    	Process p = process;
    	
    	if (p == null) {
    		// Process hasn't started yet.
    		// Count that as "running".
    		return true;
    	}
    	
        try {
            p.exitValue();
            return false;
        } catch (IllegalThreadStateException e){
            return true;
        }
    }

	/**
	 * Forcibly kill the process.
	 */
	public void killProcess() {
		logger.info("Killing process");
		process.destroy();
		
		// Important: wait for the process, otherwise we will probably create
		// a zombie process.
		boolean exited = false;
		do {
			try {
				process.waitFor();
				process.exitValue();
				exited = true;
			} catch (InterruptedException e) {
				logger.warn("Interrupted waiting for destroyed process to exit");
			} catch (IllegalThreadStateException e) {
				logger.warn("Trouble getting exit status of destroyed process", e);
			}
		} while (!exited);
		
		stdoutCollector.interrupt();
		stderrCollector.interrupt();
		if (stdinSender != null) {
			stdinSender.interrupt();
		}
	}
}
