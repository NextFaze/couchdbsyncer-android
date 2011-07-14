CREATE TABLE databases (
_id INTEGER PRIMARY KEY AUTOINCREMENT,
name TEXT,
sequence_id INTEGER,
doc_del_count INTEGER,
db_name TEXT,
url TEXT,
last_sync DATETIME,
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
revision INTEGER,
filename TEXT,
content BLOB,
length INTEGER,
content_type TEXT,
stale BOOLEAN,
UNIQUE (document_id, filename)
);

-- SPLIT

CREATE INDEX attachments_doc_id_filename_idx ON attachments(doc_id, filename);
