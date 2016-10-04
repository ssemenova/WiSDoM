const Vue = require("vue");
const App = require("./App.vue");

let app = new Vue({
    el: "#app",
    render: (h) => h(App)
});


