
# coding: utf-8

# In[24]:


get_ipython().run_cell_magic('writefile', 'mapper.py', '\nimport sys\nimport re\n\nreload(sys)\nsys.setdefaultencoding(\'utf-8\') # required to convert to unicode\n\nfor line in sys.stdin:\n    try:\n        article_id, text = unicode(line.strip()).split(\'\\t\', 1)\n    except ValueError as e:\n        continue\n    words = re.split("\\W*\\s+\\W*", text, flags=re.UNICODE)\n    for word in words:\n        print >> sys.stderr, "reporter:counter:Wiki stats,Total words,%d" % 1\n        print "%s\\t%d" % (word.lower(), 1)')


# In[ ]:


get_ipython().run_cell_magic('writefile', 'reducer.py', '\nimport sys\n\ncurrent_key = None\nword_sum = 0')


# In[ ]:


get_ipython().run_cell_magic('writefile', '-a reducer.py', '\nfor line in sys.stdin:\n    try:\n        key, count = line.strip().split(\'\\t\', 1)\n        count = int(count)\n    except ValueError as e:\n        continue\n    if current_key != key:\n        if current_key:\n            print "%s\\t%d" % (current_key, word_sum)\n        word_sum = 0\n        current_key = key\n    word_sum += count\n\nif current_key:\n    print "%s\\t%d" % (current_key, word_sum)')


# In[25]:


get_ipython().system(' hdfs dfs -ls /data/wiki')


# In[98]:


get_ipython().run_cell_magic('bash', '', '\nOUT_DIR="wordcount_result"\nNUM_REDUCERS=8\n\nhdfs dfs -rm -r -skipTrash ${OUT_DIR} > /dev/null\n\nyarn jar /opt/cloudera/parcels/CDH/lib/hadoop-mapreduce/hadoop-streaming.jar \\\n    -D mapred.jab.name="Streaming wordCount" \\\n    -D mapreduce.job.reduces=${NUM_REDUCERS} \\\n    -files mapper.py,reducer.py \\\n    -mapper "python mapper.py" \\\n    -combiner "python reducer.py" \\\n    -reducer "python reducer.py" \\\n    -input /data/wiki/en_articles_part \\\n    -output ${OUT_DIR} > /dev/null\n\nhdfs dfs -cat ${OUT_DIR}/part-00000 | head')


# In[56]:


get_ipython().run_cell_magic('writefile', 'mapper1.py', '\nimport sys\nimport re\n\nreload(sys)\nsys.setdefaultencoding(\'utf-8\') # required to convert to unicode\n\nfor line in sys.stdin:\n    try:\n        key, count = line.strip().split(\'\\t\', 1)\n        count = int(count)\n    except ValueError as e:\n        continue    \n    print "%d\\t%s"%(count,key) ')


# In[62]:


get_ipython().run_cell_magic('writefile', 'reducer1.py', '\nimport sys\n\n\nindex = 0 \n\nfor line in sys.stdin:\n    try:\n        count, key = line.strip().split(\'\\t\', 1)\n        count = int(count)\n    except ValueError as e:\n        continue  \n    index += 1\n    if index == 7:        \n        print "%s\\t%d" % (key,count)   \n        \n        \n\n\n')


# In[86]:


get_ipython().system('yarn')


# In[100]:


get_ipython().run_cell_magic('bash', '', '\nIN_DIR=wordcount_result\nOUT_DIR=wordrating_result\nNUM_REDUCERS=1\n\nhdfs dfs -rm -r -skipTrash ${OUT_DIR} > /dev/null\n\nyarn jar /opt/cloudera/parcels/CDH/lib/hadoop-mapreduce/hadoop-streaming.jar \\\n    -D mapred.job.name="Streaming wordRating" \\\n    -D mapreduce.job.reduces=${NUM_REDUCERS} \\\n    -D mapreduce.job.output.key.comparator.class=org.apache.hadoop.mapreduce.lib.partition.KeyFieldBasedComparator \\\n    -D mapreduce.partition.keycomparator.options=-nr \\\n    -files mapper1.py,reducer1.py \\\n    -mapper "python mapper1.py" \\\n    -reducer "python reducer1.py" \\\n    -input ${IN_DIR} \\\n    -output ${OUT_DIR} > /dev/null\n\nhdfs dfs -cat ${OUT_DIR}/part-00000 | head -100')


# In[75]:


get_ipython().system('hdfs dfs -cat wordrating_result/part-00000 | head')

