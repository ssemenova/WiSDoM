const axios = require("axios");
const deepcopy = require("deepcopy");

module.exports = {
    data: function () {
        return {
            waiting: true,
            results: false,
            selectedSLA: false,
            sentRequest: false
        };
    },

    props: ["mode", "templates", "frequencies", "sla"],

    computed: {
        slaSelected: function() {
            return this.correctMode() && this.selectedSLA;
        },
        
        incorrectModeMsg: function () {
            if (!this.haveTemplates())
                return "select templates first";
            
            if (!this.sla)
                return "select initial SLA first";
            
            if (!this.mode || this.mode == "rlearn")
                return "only available for supervised";
            
            if (this.frequencies.length == 0)
                return "set query frequencies first";

            return "";
        }
    },
    
    methods: {
        correctMode: function () {
            return (this.mode == "slearn")
                && this.haveTemplates()
                && this.sla != false
                && this.frequencies.length != 0;
        },

        haveTemplates: function() {
            return this.templates.length != 0;
        },

        selectSLA: function(idx) {
            if (idx == -1) {
                // select the original SLA
                this.selectedSLA = this.results.original;
                return;
            }

            this.selectedSLA = this.results.suggestions[idx];
        },

        clear: function() {
            this.selectedSLA = false;
        },

        checkMode: function() {
            if (!this.correctMode()) {
                this.results = false;
                this.waiting = true;
                this.selectedSLA = false;
                this.sentRequest = false;
                return;
            }

            if (this.sentRequest)
                return;
            this.sentRequest = true;
            
            // we are now in the correct mode. send the request...
            axios.post("/slarecs",
                       { "templates": this.templates,
                         "deadline": this.sla,
                         "frequencies": this.frequencies })
                .then(res => {
                    this.waiting = false;
                    this.results = res.data;
                });
        }
    },

    watch: {
        mode: function () { this.checkMode(); },
        sla: function() { this.checkMode(); },
        frequencies: function() { this.checkMode(); },
        templates: function() { this.checkMode(); },
        selectedSLA: function() {
            this.$emit("selected-sla-changed", deepcopy(this.selectedSLA));
        } 
    }

    
};
