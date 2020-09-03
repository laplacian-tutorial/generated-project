# **User**
**namespace:** laplacian.tutorial

user



---

## Properties

### id: `Int`
The id of this user.
- **Attributes:** *PK*

### name: `String`
The name of this user.

### username: `String`
The username of this user.

### email: `String`
The email of this user.

### phone: `String`
The phone of this user.

### website: `String`
The website of this user.

## Relationships

### address: `Address`
address
- **Cardinality:** `1`

### company: `Company?`
company
- **Cardinality:** `0..1`

### tasks: `List<Task>`
tasks
- **Cardinality:** `*`

### albums: `List<Album>`
albums
- **Cardinality:** `*`

### posts: `List<Post>`
posts
- **Cardinality:** `*`