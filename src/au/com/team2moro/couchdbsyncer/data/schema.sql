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

CREATE INDEX attachments_doc_id_filename_idx ON attachments(doc_id, filename);
CREATE INDEX databases_name_idx on databases(name);
CREATE INDEX databases_url_idx on databases(url);
CREATE INDEX documents_database_id_idx on documents(database_id);
CREATE INDEX documents_type_idx on documents(type);
CREATE INDEX documents_parent_id_idx on documents(parent_id);
CREATE INDEX attachments_document_id_idx on attachments(document_id);
CREATE INDEX attachments_filename_idx on attachments(filename);
