const axios = require("axios");

module.exports = {
    data: function () {
        return {
            strategy: false
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
            axios.post("/slearn", this.sla)
                .then(res => {
                    console.log(res);
                    this.strategy = res.data.schedule;
                });
                       
        }
    }
};
