import os
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from dotenv import load_dotenv
from langchain_community.document_loaders import DirectoryLoader, TextLoader
from langchain_text_splitters import CharacterTextSplitter
from langchain_openai import OpenAIEmbeddings, ChatOpenAI
from langchain_community.vectorstores import Chroma
from langchain.chains import RetrievalQA

# Load environment variables
load_dotenv()

app = FastAPI()

# Configuration
DATA_DIR = "./data"
PERSIST_DIRECTORY = "./db"

# Check for API Key
if not os.getenv("OPENAI_API_KEY"):
    print("WARNING: OPENAI_API_KEY is not set. RAG features will fail.")

# Initialize Vector Store (Lazy loading or persistent)
vectorstore = None

def initialize_vectorstore():
    global vectorstore
    if not os.path.exists(DATA_DIR):
        os.makedirs(DATA_DIR)
        
    # Check if we have documents
    docs = []
    if os.path.exists(DATA_DIR):
        loader = DirectoryLoader(DATA_DIR, glob="**/*.txt", loader_cls=TextLoader)
        docs = loader.load()
    
    if not docs:
        print("No documents found in data directory.")
        return

    # Split documents
    text_splitter = CharacterTextSplitter(chunk_size=1000, chunk_overlap=0)
    texts = text_splitter.split_documents(docs)

    # Create Embeddings
    embeddings = OpenAIEmbeddings()

    # Create Vector Store
    vectorstore = Chroma.from_documents(documents=texts, embedding=embeddings, persist_directory=PERSIST_DIRECTORY)
    print(f"Vector store initialized with {len(texts)} chunks.")

# Initialize on startup
@app.on_event("startup")
async def startup_event():
    try:
        initialize_vectorstore()
    except Exception as e:
        print(f"Failed to initialize vector store: {e}")

class QueryRequest(BaseModel):
    query: str

class QueryResponse(BaseModel):
    answer: str

@app.post("/rag/search", response_model=QueryResponse)
async def search(request: QueryRequest):
    global vectorstore
    if not vectorstore:
         # Try to re-initialize if empty (e.g. key was added later)
         try:
             initialize_vectorstore()
         except:
             pass
         
         if not vectorstore:
            return QueryResponse(answer="죄송합니다. 현재 AI 지식 베이스가 초기화되지 않았습니다.")

    try:
        llm = ChatOpenAI(model_name="gpt-3.5-turbo", temperature=0)
        qa_chain = RetrievalQA.from_chain_type(llm=llm, chain_type="stuff", retriever=vectorstore.as_retriever())
        
        # Run Query
        result = qa_chain.invoke(request.query)
        return QueryResponse(answer=result['result'])
    except Exception as e:
        print(f"Error processing RAG request: {e}")
        return QueryResponse(answer="죄송합니다. 답변을 생성하는 중에 오류가 발생했습니다. (API Key 확인 필요)")

@app.get("/health")
def health_check():
    return {"status": "ok"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
