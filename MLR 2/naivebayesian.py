import csv
import random
import math
import sys

def loadCsv(filename):
        lines = csv.reader(open(filename, "rb"))
        dataset = list(lines)
        for i in range(len(dataset)):
            dataset[i] = [float(x) for x in dataset[i]]
        return dataset

def splitDataset(dataset, splitRatio):
        trainSize = int(len(dataset) * splitRatio)
        trainSet = []
        copy = list(dataset)
        while len(trainSet) < trainSize:
                index = random.randrange(len(copy))
                trainSet.append(copy.pop(index))
        return [trainSet, copy]

def separateByClass(dataset):
        separated = {}
        for i in range(len(dataset)):
                vector = dataset[i]
                if (vector[-1] not in separated):
                    separated[vector[-1]] = []
                separated[vector[-1]].append(vector)
        return separated

def mean(numbers):
    return sum(numbers)/float(len(numbers))

def stdev(numbers):
        avg = mean(numbers)
        variance = sum([pow(x-avg,2) for x in numbers])/float(len(numbers)-1)
        return math.sqrt(variance)

def summarize(dataset):
        summaries = [(mean(attribute), stdev(attribute)) for attribute in zip(*dataset)]
        del summaries[-1]
        return summaries

def summarizeByClass(dataset):
        separated = separateByClass(dataset)
        summaries = {}
        for classValue, instances in separated.iteritems():
            summaries[classValue] = summarize(instances)
        return summaries

def calculateProbability(x, mean, stdev):
        exponent = math.exp(-(math.pow(x-mean,2)/(2*math.pow(stdev,2))))
        return (1 / (math.sqrt(2*math.pi) * stdev)) * exponent

def calculateClassProbabilities(summaries, inputVector):
        probabilities = {}
        for classValue, classSummaries in summaries.iteritems():
            probabilities[classValue] = 1
            for i in range(len(classSummaries)):
                        mean, stdev = classSummaries[i]
                        x = inputVector[i]
                        probabilities[classValue] *= calculateProbability(x, mean, stdev)
        return probabilities

def predict(summaries, inputVector):
        probabilities = calculateClassProbabilities(summaries, inputVector)
        bestLabel, bestProb = None, -1
        for classValue, probability in probabilities.iteritems():
            if bestLabel is None or probability > bestProb:
                    bestProb = probability
                    bestLabel = classValue
        return bestLabel

def getPredictions(summaries, testSet):
        predictions = []
        for i in range(len(testSet)):
                result = predict(summaries, testSet[i])
                predictions.append(result)
        return predictions

def getAccuracy(testSet, predictions):
        correct = 0
        for i in range(len(testSet)):
            if testSet[i][-1] == predictions[i]:
                correct += 1
        return (correct/float(len(testSet))) * 100.0


def main():
    data_dict = {}
    model = "LDA"
    
    model = sys.argv[1]
    
    if model == "LDA":
        op_file = "Output.txt"
    elif model == "M4":
        op_file = "Output2.txt"
    elif model == "blockHMM":
        op_file = "Output1.txt"

    attribute = sys.argv[2]



    #read annotation csv file
    f = open('AnnotatedData.csv', 'rb') # opens the csv file
    try:
        reader = csv.reader(f)  # creates the reader object
        for row in reader:   # iterates the rows of the file in orders
            data_dict[row[0]] = row;
    finally:
        f.close()      # closing

    #open text file to write
    of = open("ex1data3.csv", "w")

    # read output txt file
    f = open(op_file, 'r')
    data = f.readlines()
    n = 0

    for line in data:
        words = line.split()
        key = words[0];
        topic_dist = [0.0] * 6
        
        if(model != "blockHMM"):
            for i in range(3, len(words)):
                word_topic = words[i].split(':')
                #print key,word_topic[0],word_topic[1]
                topic_dist[int(word_topic[1])] += 1
            for i in range(0, len(topic_dist)):
                #print topic_dist[i]
                topic_dist[i] = float(topic_dist[i] / len(words))
    
        row = data_dict[key]
        if attribute == "offtask":
            if row[8] == "no":
                value = 0
            elif row[8] == "pre":
                value = 1
            else:
                value = 2
        elif attribute == "cheating":
            if row[9] == "no":
                value = 0
            elif row[9] == "pre":
                value = 1
            else:
                value = 2
        elif attribute == "at":
            if row[10] == "no":
                value = 0
            else:
                value = 1
        elif attribute == "reas":
            if row[11] == "no":
                value = 0
            else:
                value = 1
        elif attribute == "trans":
            if row[12] == "no":
                value = 0
            elif row[12] == "ex":
                value = 1
            else:
                value = 2
        elif attribute == "neg":
            if row[13] == "a1":
                value = 1
            elif row[13] == "a2":
                value = 2
            elif row[13] == "k1":
                value = 3
            elif row[13] == "k2":
                value = 4
            else:
                value = 0
        elif attribute == "het":
            if row[14] == "hc":
                value = 1
            elif row[14] == "he":
                value = 2
            elif row[14] == "m":
                value = 3
            else:
                value = 0

        if model == "blockHMM":
            of.write(str(words[3]) + "," + str(value)+"\n")
        else:
            of.write(str(topic_dist[1])+","+str(topic_dist[2])+","+str(topic_dist[3])+","+str(topic_dist[4])+","+str(topic_dist[5])+","+str(value)+"\n")

        n += 1

    f.close()
    of.close()

    filename = 'ex1data3.csv'
    splitRatio = 0.8
    dataset = loadCsv(filename)
    trainingSet, testSet = splitDataset(dataset, splitRatio)
    print('Split {0} rows into train={1} and test={2} rows').format(len(dataset), len(trainingSet), len(testSet))
    sum = 0
    for i in range(1,100):
        # prepare model
        summaries = summarizeByClass(trainingSet)
        # test model
        predictions = getPredictions(summaries, testSet)
        accuracy = getAccuracy(testSet, predictions)
        sum += accuracy
    accuracy = sum / 100
    
    print('Accuracy: {0}%').format(accuracy)
    print('Kappa: {0}%').format(((accuracy/100)-0.5)/0.5)

main()