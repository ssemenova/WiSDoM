const axios = require("axios");

module.exports = {
    data: function () {
        return {
            strategy: false,
            waiting: true
        };
    },

    props: ["sla", "mode"],

    computed: {
        haveData: function() {
            return this.strategy !== false;
        }
    },

    methods: {
        isSLearn: function() {
            return this.mode == "slearn";
        }
    },

    watch: {
        sla: function () {
            this.waiting = true;
            console.log(JSON.stringify(this.sla));
            axios.post("/slearn", this.sla)
                .then(res => {
                    this.strategy = res.data.schedule;
                    this.waiting = false;
                });

        }
    }
};
