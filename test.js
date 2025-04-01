fetch("http://192.168.150.163/api/v1/connection-fees/filter?page=1&size=20", {
    "headers": {
        "accept": "application/json",
        "accept-language": "en-US,en;q=0.9",
        "authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6ImFkbWluQGdtYWlsLmNvbSIsImxvZ091dCI6ZmFsc2UsImlhdCI6MTc0MzE2NzEwNiwiZXhwIjoxNzQzMjUzNTA2fQ.lLPjrteoyXaZrhV7q6kIZZY0h-sbAaORGzEKpw61n8Q",
        "ngrok-skip-browser-warning": "true",
        //"cookie": "auth_token=eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6ImFkbWluQGdtYWlsLmNvbSIsImxvZ091dCI6ZmFsc2UsImlhdCI6MTc0MzE2NzEwNiwiZXhwIjoxNzQzMjUzNTA2fQ.lLPjrteoyXaZrhV7q6kIZZY0h-sbAaORGzEKpw61n8Q",
        "Referer": "http://192.168.150.163/",
        "Referrer-Policy": "strict-origin-when-cross-origin"
    },
    "body": null,
    "method": "GET"
}).then(res => res.data).then(res => console.log(res.data));