const axios = require("axios");

module.exports = {
    data: function () {
        return {
            waiting: true
        };
    },

    props: ["mode", "templates", "sla"],

    computed: {
        correctMode: function () {
            return this.mode == "slearn" && this.haveTemplates() && this.sla;
        },

        incorrectModeMsg: function () {
            if (!this.mode || this.mode == "rlearn")
                return "only available for supervised";

            if (!this.haveTemplates())
                return "select templates first";

            if (!this.sla)
                return "select initial SLA first";

            return "";
        }
    },
    
    methods: {
        haveTemplates: function() {
            return this.templates
                .map((itm, idx) => (itm != null) && idx)
                .length != 0;
        }
    },

    watch: {
        correctMode: function(newVal) {
            if (!newVal)
                return;

            
        }
    }

    
};
