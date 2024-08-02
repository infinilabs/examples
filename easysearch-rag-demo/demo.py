import os

# 导入 Document Loaders
from langchain_community.document_loaders import PyPDFLoader

# 1. Load Pdf
base_dir = './easysearch' # 文档的存放目录
docs = []
for file in os.listdir(base_dir): 
    file_path = os.path.join(base_dir, file)
    if file.endswith('.pdf'):
        loader = PyPDFLoader(file_path)
        docs.extend(loader.load())
    

# 2.将 docs 切分成块
from langchain.text_splitter import RecursiveCharacterTextSplitter
text_splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=20)
chunked_documents = text_splitter.split_documents(docs)


# 3.定义 embedding 模型
from langchain_community.embeddings import OllamaEmbeddings
ollama_emb = OllamaEmbeddings(
    model="mxbai-embed-large",
)

# 4. 定义 easysearch 集群的信息，以及存放向量的索引名称
from langchain_community.vectorstores import EcloudESVectorStore
ES_URL = "https://127.0.0.1:9200"
USER = "admin"
PASSWORD = "5c6cc0f7905905d8c65a"
indexname = "infini"

docsearch = EcloudESVectorStore.from_documents(
    chunked_documents,
    ollama_emb,
    es_url=ES_URL,
    user=USER,
    password=PASSWORD,
    index_name=indexname,
    verify_certs=False,
)

# 5. Retrieval 准备模型和Retrieval链
import logging 
# MultiQueryRetriever工具
from langchain.retrievers.multi_query import MultiQueryRetriever 
# RetrievalQA链
from langchain.chains import RetrievalQA 

# # 设置Logging
logging.basicConfig()
logging.getLogger('langchain.retrievers.multi_query').setLevel(logging.INFO)

# # 实例化一个大模型工具 
from langchain_community.chat_models import ChatOllama
llm = ChatOllama(model="qwen2:latest")

from langchain.prompts import PromptTemplate
my_template = PromptTemplate( 
    input_variables=["question"],
    template="""You are an AI language model assistant. Your task is 
    to generate 3 different versions of the given user 
    question in Chinese to retrieve relevant documents from a vector  database. 
    By generating multiple perspectives on the user question, 
    your goal is to help the user overcome some of the limitations 
    of distance-based similarity search. Provide these alternative 
    questions separated by newlines. Original question: {question}""",
)



# # 实例化一个MultiQueryRetriever
retriever_from_llm = MultiQueryRetriever.from_llm(retriever=docsearch.as_retriever(), llm=llm,prompt=my_template,include_original=True)

# # 实例化一个RetrievalQA链
qa_chain = RetrievalQA.from_chain_type(llm,retriever=retriever_from_llm)

# 6. Output 问答系统的UI实现
from flask import Flask, request, render_template
app = Flask(__name__) # Flask APP

@app.route('/', methods=['GET', 'POST'])
def home():
    if request.method == 'POST':

        # 接收用户输入作为问题
        question = request.form.get('question')        
        
        # RetrievalQA链 - 读入问题，生成答案
        result = qa_chain({"query": question})
        
        # 把大模型的回答结果返回网页进行渲染
        return render_template('index.html', result=result)
    
    return render_template('index.html')

if __name__ == "__main__":
    app.run(host='0.0.0.0',debug=True,port=5001)