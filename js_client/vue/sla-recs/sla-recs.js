const axios = require("axios");
const deepcopy = require("deepcopy");

module.exports = {
    data: function () {
        return {
            waiting: true,
            results: false,
            selectedSLA: false,
            sentRequest: false,
            
            frequencies: {"1": 5, "2": 5, "3": 5, "4": 5, "5": 5}
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

        validateAll: function() {
            return [1,2,3,4,5].every((idx) => {
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
            const toR = [];
            for (let i = 1; i <= 5; i++) {
                if (this.templates.indexOf(i) == -1)
                    continue;

                toR.push(parseInt(this.frequencies[i]));
            }


            return toR;
        },

        clear: function() {
            this.selectedSLA = false;
            this.$emit("selected-sla-changed", false);
            this.$emit("frequency-changed", false);
        },
        
        save: function () {
            if (!this.validateAll())
                return;

            console.log("freqs: " + JSON.stringify(this.getFreqs()));
            
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
        slaType: function() { this.checkMode(); }

    },

    created: function() {
        this.clear();
        this.checkMode();

        // for some reason using the regular watch doesn't work...
        this.$watch("frequencies", function(oldV, newV) {
            this.save();
        }, {deep: true});
    }


};
