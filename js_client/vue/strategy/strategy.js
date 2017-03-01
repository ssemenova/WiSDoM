const axios = require("axios");

module.exports = {
    data: function () {
        return {
            strategy: false,
            waiting: true,
            firstImg: true,
            lastImg: false,
            imgNumber: 1,
            decisionTreePng: './assets/decision-tree/Slide1.png',
            dt: false
        };
    },

    props: ["sla"],

    computed: {
        haveData: function() {
            return this.strategy !== false;
        }
    },

    methods: {

        loadDT: function() {
            axios.post("/tree", this.sla)
                .then(res => {
                    this.dt = res.data.tree;
                    $("#dtModal").modal('show');
                });

        },
        
        loadNext: function() {

            console.log(JSON.stringify(this.sla));
            
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
                this.strategy = false;
                return;
            }

            axios.post("/slearn", this.sla)
                .then(res => {
                    this.strategy = res.data.schedule;
                    this.waiting = false;
                });

        }
    },

    watch: {
        sla: function () {
            this.checkSLA();
        }
    }

};
