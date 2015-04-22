from numpy import loadtxt, zeros, ones, array, linspace, logspace, mean, std, arange
import csv
import sys
from nltk.corpus import stopwords
cachedStopWords = stopwords.words("english")

def feature_normalize(X):

    mean_r = []
    std_r = []
    
    X_norm = X
    
    n_c = X.shape[1]
    for i in range(n_c):
        m = mean(X[:, i])
        s = std(X[:, i])
        mean_r.append(m)
        std_r.append(s)
        X_norm[:, i] = (X_norm[:, i] - m) / s
    
    return X_norm, mean_r, std_r


def compute_cost(X, y, theta):

    m = y.size
    
    predictions = X.dot(theta)
    
    sqErrors = (predictions - y)
    
    J = (1.0 / (2 * m)) * sqErrors.T.dot(sqErrors)
    
    return J


def gradient_descent(X, y, theta, alpha, num_iters):

    m = y.size
    J_history = zeros(shape=(num_iters, 1))
    
    for i in range(num_iters):
        
        predictions = X.dot(theta)
        
        theta_size = theta.size
        
        for it in range(theta_size):
            
            temp = X[:, it]
            temp.shape = (m, 1)
            
            errors_x1 = (predictions - y) * temp
            
            theta[it][0] = theta[it][0] - alpha * (1.0 / m) * errors_x1.sum()
        
        J_history[i, 0] = compute_cost(X, y, theta)
    
    return theta, J_history

def predict(x, mean_r, std_r, num):
    temp = [1.0]
    for i in range(0,num):
        temp.append(((x[i] - mean_r[i]) / std_r[i]))
    return array(temp)

data_dict = {}
    
model = sys.argv[1]
    
if model == "LDA":
    op_file = "Output.txt"
    num = 5
elif model == "M4":
    op_file = "Output2.txt"
    num = 5
elif model == "blockHMM":
    op_file = "Output1.txt"
    num = 1

attribute = sys.argv[2]

#read annotation csv file
f = open('AnnotatedData.csv', 'rb') # opens the csv file
try:
    reader = csv.reader(f)  # creates the reader object
    for row in reader:   # iterates the rows of the file in orders
        #data_list = row.split(',');
        data_dict[row[0]] = row;
finally:
    f.close()      # closing

#open text file to write
of = open("ex1data1.txt", "w")
oof = open("ex1data2.txt", "w")
ooof = open("wordrankings" + model + ".txt", "w")

# read output txt file
f = open(op_file, 'r')
data = f.readlines()
n = 0

topic_word_rankings = {}


for line in data:
    words = line.split()
    
    key = words[0];
    topic_dist = [0.0] * (num+1)
    row = data_dict[key]
    if row[5] == "Tutor":
        row[5] = "Tutor"
    else:
        row[5] = "Student"

    if model != "blockHMM":
        for i in range(3, len(words)):
            word_topic = words[i].split(':')
            topic_dist[int(word_topic[1])] += 1

            if not topic_word_rankings.has_key(row[3]):
                topic_word_rankings[row[3]] = {}
            elif not topic_word_rankings[row[3]].has_key(row[4]):
                topic_word_rankings[row[3]][row[4]] = {}
            elif not topic_word_rankings[row[3]][row[4]].has_key(row[5]):
                topic_word_rankings[row[3]][row[4]][row[5]] = {}
            elif not topic_word_rankings[row[3]][row[4]][row[5]].has_key(word_topic[1]):
                topic_word_rankings[row[3]][row[4]][row[5]][word_topic[1]] = {}
            elif not topic_word_rankings[row[3]][row[4]][row[5]][word_topic[1]].has_key(word_topic[0]):
                topic_word_rankings[row[3]][row[4]][row[5]][word_topic[1]][word_topic[0]] = 1
            else:
                topic_word_rankings[row[3]][row[4]][row[5]][word_topic[1]][word_topic[0]] += 1

        for i in range(0, len(topic_dist)):
            topic_dist[i] = float(topic_dist[i] / len(words))
                
    else:
        for i in range(4, len(words)):
            word_topic = words[i].split(':')
            
            
            if not topic_word_rankings.has_key(row[3]):
                topic_word_rankings[row[3]] = {}
            elif not topic_word_rankings[row[3]].has_key(row[4]):
                topic_word_rankings[row[3]][row[4]] = {}
            elif not topic_word_rankings[row[3]][row[4]].has_key(row[5]):
                topic_word_rankings[row[3]][row[4]][row[5]] = {}
            elif not topic_word_rankings[row[3]][row[4]][row[5]].has_key(words[3]):
                topic_word_rankings[row[3]][row[4]][row[5]][words[3]] = {}
            elif not topic_word_rankings[row[3]][row[4]][row[5]][words[3]].has_key(word_topic[0]):
                topic_word_rankings[row[3]][row[4]][row[5]][words[3]][word_topic[0]] = 1
            else:
                topic_word_rankings[row[3]][row[4]][row[5]][words[3]][word_topic[0]] += 1




    if attribute == "at":
        if row[10] == "no":
            value = 0
        else:
            value = 1
    elif attribute == "reas":
        if row[11] == "no":
            value = 0
        else:
            value = 1


    if n<761:
        if model == "blockHMM":
            of.write(str(words[3]+","+str(value)+"\n"))
        else:
            of.write(str(topic_dist[1])+","+str(topic_dist[2])+","+str(topic_dist[3])+","+str(topic_dist[4])+","+str(topic_dist[5])+","+str(value)+"\n")
    else:
        if model == "blockHMM":
            oof.write(str(words[3]+","+str(value)+"\n"))
        else:
            oof.write(str(topic_dist[1])+","+str(topic_dist[2])+","+str(topic_dist[3])+","+str(topic_dist[4])+","+str(topic_dist[5])+","+str(value)+"\n")
    n += 1


for section in topic_word_rankings.keys():
    for condition in topic_word_rankings[section].keys():
        for role in topic_word_rankings[section][condition].keys():
            for topic in topic_word_rankings[section][condition][role].keys():
                ooof.write(section + " " + condition + " " + role + " " + "Topic" + topic + "\n\n")
                temp = []
                for word in topic_word_rankings[section][condition][role][topic].keys():
                    temp.append((int(topic_word_rankings[section][condition][role][topic][word]),word))
                temp.sort(reverse = True)

                string = ""
                for a,b in temp:
                    string = string + " " + b
                
                string = ' '.join([word for word in string.split() if word not in cachedStopWords])
                ooof.write(string + "\n\n")




f.close()
of.close()
oof.close()
ooof.close()

#Load the dataset
data = loadtxt('ex1data1.txt', delimiter=',')



X = data[:, :num]
y = data[:, num]

#print y

#sys.exit()

#number of training samples
m = y.size

y.shape = (m, 1)

#Scale features and set them to zero mean
x, mean_r, std_r = feature_normalize(X)

#Add a column of ones to X (interception data)
it = ones(shape=(m, num+1))
it[:, 1:num+1] = x

#Some gradient descent settings
iterations = 1000
alpha = 0.001

#Init Theta and Run Gradient Descent
theta = zeros(shape=(num+1, 1))

theta, J_history = gradient_descent(it, y, theta, alpha, iterations)
print theta

test_data = loadtxt('ex1data2.txt', delimiter=',')


xop = []
op = []


for x in test_data:
    temp = predict(x, mean_r, std_r, num)
    prediction = temp.dot(theta)
    xop.append(prediction[0])
    op.append(x[num])
#print prediction[0], x[5]

initial = 1.0
delta = 0.001
max_accuracy = 0.0
max_score = 0.0
max_acc = []
score = []

while initial > 0.0  and initial <= 1:
    correct = 0.0
    ap = 0.0
    tp = 0.0
    p = 0.0
    f_score = 0.0
    recall = 0.0
    precision = 0.0
    
    for i in range(len(xop)):
        #print op[i], xop[i], "*"
        if xop[i] < initial:
            check = 0.0
        else:
            check = 1.0
            p += 1.0
        
        #print op[i], xop[i]
        if int(op[i]) == 1:
            ap += 1.0
        if int(op[i]) == int(check):
            correct += 1.0
            if int(check) == 1:
                tp += 1.0
    accuracy = float(correct/len(xop))
    if ap!=0 and p!=0 and tp!=0:
        
        recall = tp/ap
        precision = tp/p
        f_score = 2 * (precision * recall)/(precision + recall)
    
    #print accuracy, initial, correct, len(xop)
    if accuracy >= max_accuracy :
        max_accuracy = accuracy
        kappa = (max_accuracy - 0.5)/0.5
        max_acc = [max_accuracy, initial, correct, len(xop), kappa]

    if f_score >= max_score :
        max_score = f_score
        score = [max_score, recall, precision, initial, correct, len(xop)]
    
    initial = initial - delta

print "Accuracy : ", max_acc[0]
print "Kappa : " , max_acc[4]
print "F measure : ", score[0]
print "Recall : ", score[1]
print "Precision : ", score[2]


