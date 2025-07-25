document.addEventListener('DOMContentLoaded', function() {
    const feedbackForm = document.getElementById('feedback-form');

    // Check if the form exists on the page
    if (feedbackForm) {
        const feedbackMessageDiv = document.getElementById('feedbackMessage');
        const feedbackComment = document.getElementById('feedback-comment');
        const ratingInputs = document.querySelectorAll('input[name="rating"]');

        // Get CSRF token. These will be null if the meta tags don't exist.
        const csrfTokenEl = document.querySelector("meta[name='_csrf']");
        const csrfHeaderEl = document.querySelector("meta[name='_csrf_header']");

        feedbackForm.addEventListener('submit', function(event) {
            event.preventDefault();

            // FIX #1: Check for CSRF token existence before proceeding
            if (!csrfTokenEl || !csrfHeaderEl) {
                feedbackMessageDiv.textContent = 'Error: Cannot submit form. Security token is missing. Please refresh and log in.';
                feedbackMessageDiv.style.color = 'red';
                return;
            }

            const csrfToken = csrfTokenEl.content;
            const csrfHeader = csrfHeaderEl.content;

            const selectedRating = document.querySelector('input[name="rating"]:checked');
            // FIX #2: Use null instead of 0 for no rating
            const rating = selectedRating ? parseInt(selectedRating.value) : null;
            const comment = feedbackComment.value.trim();

            // FIX #2 (Part 2): Update validation to check for null
            if (rating === null && comment === "") {
                feedbackMessageDiv.textContent = 'Please provide a rating or a comment.';
                feedbackMessageDiv.style.color = 'orange';
                return;
            }

            feedbackMessageDiv.textContent = 'Submitting feedback...';
            feedbackMessageDiv.style.color = 'gray';

            const feedbackData = {
                rating: rating,
                comment: comment
            };

            fetch('/api/feedback', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify(feedbackData)
            })
                .then(response => {
                    if (!response.ok) {
                        return response.json().then(errorData => {
                            throw new Error(errorData.message || 'Failed to submit feedback.');
                        }).catch(() => {
                            //throw new Error('Failed to submit feedback (server returned non-JSON response).');
                            throw new Error('Rating or comment is required.');
                        });
                    }
                    return response.json();
                })
                .then(data => {
                    feedbackMessageDiv.textContent = 'Thank you for your feedback!';
                    feedbackMessageDiv.style.color = 'green';
                    feedbackComment.value = '';
                    ratingInputs.forEach(input => input.checked = false);
                })
                .catch(error => {
                    console.error('Error submitting feedback:', error);
                    feedbackMessageDiv.textContent =error.message;
                    feedbackMessageDiv.style.color = 'red';
                });
        });
    }
});