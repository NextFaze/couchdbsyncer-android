CREATE TABLE databases (
_id INTEGER PRIMARY KEY AUTOINCREMENT,
name TEXT,
sequence_id INTEGER,
doc_del_count INTEGER,
doc_update_seq INTEGER,
db_name TEXT,
url TEXT,
username TEXT,
password TEXT
);

-- SPLIT

CREATE TABLE documents (
_id INTEGER PRIMARY KEY AUTOINCREMENT,
doc_id TEXT,          
database_id INTEGER,  
revision TEXT,
content TEXT,         
object BLOB,        
parent_id TEXT,
type TEXT,
tags TEXT
);

-- SPLIT

CREATE TABLE attachments (
_id INTEGER PRIMARY KEY AUTOINCREMENT,
document_id INTEGER, 
doc_id TEXT,
content BLOB,
length INTEGER,
content_type TEXT,
filename TEXT,
unfetched_changes BOOLEAN
);
