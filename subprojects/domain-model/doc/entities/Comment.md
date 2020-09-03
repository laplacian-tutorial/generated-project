# **Comment**
**namespace:** laplacian.tutorial

comment



---

## Properties

### post_id: `Int`
The post_id of this comment.
- **Attributes:** *PK*

### seq_number: `Int`
The seq_number of this comment.
- **Attributes:** *PK*

### name: `String`
The name of this comment.

### email: `String`
The email of this comment.

### body: `String`
The body of this comment.

## Relationships

### post: `Post`
post
- **Cardinality:** `1`