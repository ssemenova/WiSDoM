function url(s) {
    const l = window.location;
    return ((l.protocol === "https:") ? "wss://" : "ws://") + l.host + s;
}

const d = document.getElementById("data");
const socket = new WebSocket(url("/bandit"));

socket.onmessage = function (event) {
    console.log(event.data);
};
