from nltk.corpus import stopwords
import csv
import sys
import math

def cosine_similarity(v1,v2):
    "compute cosine similarity of v1 to v2: (v1 dot v1)/{||v1||*||v2||)"
    sumxx, sumxy, sumyy = 0, 0, 0
    for i in range(len(v1)):
        x = v1[i]; y = v2[i]
        sumxx += x*x
        sumyy += y*y
        sumxy += x*y
    return sumxy/math.sqrt(sumxx*sumyy)

cachedStopWords = stopwords.words("english")

data_dict = []

for i in range(0,6):
    data_dict.append([])

data_prob = []
for i in range(0,6):
    data_prob.append({})

data_sum = [0] * 6



model = sys.argv[1]


if model == "LDA":
    ip_file = "TopicWordWeights.csv"
    op_file = "TopicWordRankingsLDA"
elif model == "M4":
    ip_file = "TopicWordWeights1.csv"
    op_file = "TopicWordRankingsM4"
elif model == "blockHMM":
    ip_file = "TopicWordWeights2.csv"
    op_file = "TopicWordRankingsblockHMM"


f = open(ip_file, 'rb') # opens the csv file
try:
    reader = csv.reader(f)  # creates the reader object
    skip = 1
    for row in reader:   # iterates the rows of the file in orders
        if skip:
            skip = 0
            continue
        data_dict[1].append((float(row[1]),row[0]))
        data_dict[2].append((float(row[2]),row[0]))
        data_dict[3].append((float(row[3]),row[0]))
        data_dict[4].append((float(row[4]),row[0]))
        data_dict[5].append((float(row[5]),row[0]))
finally:
    f.close()      # closing

of = open(op_file + ".txt", "w")
oof = open(op_file + "WithoutStopWords.txt", "w")

for i in range(1,len(data_dict)):
    
    temp = data_dict[i]
    temp.sort(reverse = True)

    top = 10000
    string = ""
    sum = 0
    for a,b in temp:
        if top==0:
            break
        string = string + " " + b
        data_prob[i][b]=a
        sum += a*a
        top -= 1
    data_sum[i] = sum
    of.write("#######################################" + "Topic " + str(i) + ":" + "############################\n")
    of.write(string + "\n")
    of.write("##################################################################################################" + "\n")

    string = ' '.join([word for word in string.split() if word not in cachedStopWords])

    oof.write("#######################################" + "Topic " + str(i) + ":" + "###########################" + "\n")
    oof.write(string + "\n")
    oof.write("#################################################################################################" + "\n")

ooof = open("newdialogueTopics_" + model + ".txt", "w")

# read output txt file
f = open("input.txt", 'r')
data = f.readlines()


for line in data:
    line = ' '.join([word for word in line.split() if word not in cachedStopWords])
    words = line.split()
    word_dist = {}
    cosine_score = [0] * 6
    sum = 0
    max = 0
    topic = 0
    for word in words:
        if not word_dist.has_key(word):
            word_dist[word] = 1
        else:
            word_dist[word] += 1
    for word in word_dist.keys():
        word_dist[word] = float(word_dist[word])/len(words)
        sum += (word_dist[word]) * (word_dist[word])

        for i in range(1,6):
            if(data_prob[i].has_key(word)):
                cosine_score[i] = data_prob[i][word] * word_dist[word]

    for i in range(1,6):
        cosine_score[i] /= (sum + data_sum[i])

        if(cosine_score[i] > max):
            max = cosine_score[i]
            topic = i

    ooof.write(str(topic) + " " + str(max) + " " + line + "\n")

ooof.close()











