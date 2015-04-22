/*
 * Web C programming environment
 * Copyright (c) 2010-2011, David H. Hovemeyer <david.hovemeyer@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cloudcoder.builder2.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;

/**
 * Start a thread to collect all of the data from a given
 * input stream (which could be the output of a process.)
 * The data is stored as a List of Strings, one per line of output.
 */
public class OutputCollector implements IOutputCollector {
	private InputStream inputStream;
	private Thread readerThread;
	private List<String> collectedOutput;
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.submitsvc.oop.builder.IOutputCollector#interrupt()
	 */
	@Override
	public void interrupt() {
	    readerThread.interrupt();
	}

	public OutputCollector(InputStream inputStream) {
		this.inputStream = inputStream;
		this.collectedOutput = new LinkedList<String>();
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.submitsvc.oop.builder.IOutputCollector#start()
	 */
	@Override
	public void start() {
		readerThread = new Thread(new Runnable() {
			@Override
			public void run() {
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				try {
					for (;;) {
						String line = reader.readLine();
						if (line == null) {
							break;
						}
						collectedOutput.add(line);
					}
				} catch (IOException e) {
					// ignore
				} finally {
					IOUtils.closeQuietly(reader);
				}
			}
		});

		readerThread.start();
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.submitsvc.oop.builder.IOutputCollector#getCollectedOutput()
	 */
	@Override
	public List<String> getCollectedOutput() {
		return Collections.unmodifiableList(collectedOutput);
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.submitsvc.oop.builder.IOutputCollector#join()
	 */
	@Override
	public void join() throws InterruptedException {
		readerThread.join();
	}

}
