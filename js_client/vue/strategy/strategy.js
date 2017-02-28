const axios = require("axios");

module.exports = {
    data: function () {
        return {
            strategy: false,
            waiting: true,
            firstImg: true,
            lastImg: false,
            imgNumber: 1,
            decisionTreePng: './assets/decision-tree/Slide1.png'
        };
    },

    props: ["sla"],

    computed: {
        haveData: function() {
            return this.strategy !== false;
        }
    },

    methods: {

        loadNext: function() {
            if (this.imgNumber <= 16) {
                this.imgNumber++;
                this.firstImg = false;
                if (this.imgNumber == 17) {
                    this.lastImg = true;
                }
                this.decisionTreePng = './assets/decision-tree/Slide' + this.imgNumber + '.png';
                //load previous button doesn't work yet lol
            }
        },

        checkSLA: function() {
            this.waiting = true;
            if (!this.sla) {
                console.log("sla is false, clearing!");
                this.strategy = false;
                return;
            }

            console.log("sending request...");
            axios.post("/slearn", this.sla)
                .then(res => {
                    console.log("got response");
                    this.strategy = res.data.schedule;
                    this.waiting = false;
                });

        }
    },

    watch: {
        sla: function () {
            console.log("got an sla change: " + JSON.stringify(this.sla));
            this.checkSLA();
        }
    }

};
