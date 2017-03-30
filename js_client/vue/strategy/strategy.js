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
            dt: false,
            dturl: ""
        };
    },

    props: ["sla", "templates"],

    computed: {
        haveData: function() {
            return this.strategy !== false;
        }
    },

    methods: {

        loadDT: function() {
          if (this.sla.deadline === 69 && this.templates.includes(1) && this.templates.includes(5) && this.templates.length == 2) {
            this.dturl = "./assets/decision-tree/example1.png";
            $("#dtModal").modal('show');
          } else if (this.sla.deadline === 70 && this.templates.includes(1) && this.templates.includes(5) && this.templates.length == 2) {
            this.dturl = "./assets/decision-tree/example2.png";
            $("#dtModal").modal('show');
          } else {
            axios.post("/tree", this.sla)
                .then(res => {
                    this.dt = res.data.tree;
                    this.dturl = '/tree/' + this.sla.sessionID;
                    $("#dtModal").modal('show');
                });
          }

          console.log(this.templates.length);
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

            var panel = $('#results');
            panel.addClass('big-hacky-panel');

            axios.post("/slearn", this.sla)
                .then(res => {
                    this.strategy = res.data.schedule;
                    this.waiting = false;
                    panel.removeClass('big-hacky-panel');
                });

        }
    },

    watch: {
        sla: function () {
            this.checkSLA();
        }
    }

};
