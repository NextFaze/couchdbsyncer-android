CREATE TABLE databases (
_id INTEGER PRIMARY KEY AUTOINCREMENT,
name TEXT,
sequence_id INTEGER,
doc_del_count INTEGER,
doc_update_seq INTEGER,
db_name TEXT,
url TEXT,
UNIQUE (name)
);

-- SPLIT

CREATE TABLE documents (
_id INTEGER PRIMARY KEY AUTOINCREMENT,
database_id INTEGER,  
doc_id TEXT,
revision TEXT,
content BLOB,        
parent_id TEXT,
type TEXT,
tags TEXT,
UNIQUE (database_id, doc_id)
);

-- SPLIT

CREATE TABLE attachments (
_id INTEGER PRIMARY KEY AUTOINCREMENT,
document_id INTEGER, 
doc_id TEXT,
filename TEXT,
content BLOB,
length INTEGER,
content_type TEXT,
stale BOOLEAN,
UNIQUE (document_id, filename)
);
