const axios = require("axios");
const deepcopy = require("deepcopy");

module.exports = {
    data: function () {
        return {
            waiting: true,
            results: false,
            selectedSLA: false,
            sentRequest: false,
            
            frequencies: [5,5,5,5,5,5,5,5,5,5,5]
        };
    },

    props: ["mode", "templates", "sla", "slaType"],

    computed: {
        slaSelected: function() {
            return this.correctMode() && this.selectedSLA;
        },

        canSave: function () {
            return this.selectedSLA != false;
        }
    },

    methods: {
        validateFreq: function(idx) {
            return (/^[0-9]+$/.test(this.frequencies[idx]));
        },

        validateAll: function(idx) {
            return this.frequencies.every((i, idx) => {
                return this.validateFreq(idx);
            });
        },
        
        correctMode: function () {
            return (this.mode == "slearn")
                && this.sla != false
                && this.frequencies.length != 0;
        },

        selectSLA: function(idx) {
            if (idx == -1) {
                // select the original SLA
                this.selectedSLA = this.results.original;
                this.selectedSLA.index = -1;
                this.save();
                return;
            }

            this.selectedSLA = this.results.suggestions[idx];
            this.selectedSLA.index = idx;
            this.save();
        },

        checkMode: function() {
            if (!this.correctMode()) {
                this.results = false;
                this.waiting = true;
                this.selectedSLA = false;
                this.sentRequest = false;
                this.clear();
                return;
            }

            if (this.sentRequest)
                return;

            this.sentRequest = true;

            // we are now in the correct mode. send the request...
            axios.post("/slarecs",
                       { "templates": this.templates,
                         "deadline": this.sla,
                         "frequencies": this.getFreqs() })
                .then(res => {
                    this.waiting = false;
                    this.results = res.data;
                    this.selectSLA(-1);
                });
        },

        getFreqs: function() {
            return this.frequencies
                .filter((itm, idx) => this.templates.indexOf(idx) > -1);
        },

        clear: function() {
            this.selectedSLA = false;
            this.$emit("selected-sla-changed", false);
            this.$emit("frequency-changed", false);
        },
        
        save: function () {
            if (!this.validateAll())
                return;
            
            axios.post("/frequency",
                       {"sessionID": this.selectedSLA.sessionID,
                        "frequencies": this.getFreqs()})
                .then(() => {
                    this.$emit("selected-sla-changed", deepcopy(this.selectedSLA));
                });
            
        },

        isPerQuery: function () {
            return this.slaType == "Per Query";
        }
    },

    watch: {
        mode: function () { this.checkMode(); },
        sla: function() { this.checkMode(); },
        templates: function() { this.checkMode(); },
        frequencies: function() { this.save(); }
    },

    created: function() {
        this.checkMode();
    }


};
