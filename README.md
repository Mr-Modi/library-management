Description Build a backend API for managing a library’s book inventory. The system
should handle standard book management and also process notifications for users when a
wishlisted book is returned.


Requirements
● CRUD Operations for Books:
○ Create: Add a new book with fields like title, author, isbn,
publishedYear, and availabilityStatus.
○ Read: Retrieve a paginated list of books with optional filters (e.g., by author,
publishedYear).
○ Update: Edit book details. A key use case is updating
availabilityStatus when a book is borrowed or returned.
○ Delete: Remove a book from the inventory.
● Search: Implement an endpoint to search for books by title or author using
partial matching.
● Validation: Ensure no duplicate isbn and that publishedYear is a valid year.
● Asynchronous Wishlist Notification: When a book's status is updated from
"Borrowed" to "Available", the API must trigger a background task. This task will find
all users who have wishlisted that specific book and log a notification message for
each (e.g., "Notification prepared for user_id: Book [Title] is
now available."). The API call that updates the book's status must return a
response quickly, without waiting for this notification logic to complete.

Evaluation Criteria
● Clean API design (RESTful principles, proper status codes).
● Efficient database queries and schema design.
● Error handling and input validation.
● Knowledge of pagination and search optimization.
● Audit Trail: Implement soft deletes for books.
● Asynchronous Processing: A clear and logical implementation of the decoupled
notification task.