const axios = require("axios");
const Plotly = require("plotly.js/lib/core");
Plotly.register([
    require('plotly.js/lib/bar')
]);

const slider = require("./slider.vue");

const _ = {
    max: require("lodash/max"),
    min: require("lodash/min"),
    sum: require("lodash/sum")
};


module.exports = {
    components: {
        slider
    },
    
    data: function () {
        return {
            groups: [],
            latencies: [],
            deadline: 150,
            minValue: 50,
            maxValue: 150,
            saved: false
        };
    },

    props: ['templates'],
    methods: {
        getLatencyInfo: function () {
            axios.get("/querylatency")
                .then(r => {
                    this.latencies = r.data;
                });
        },

        haveTemplates: function() {
            return this.templates.length != 0;
        },

        save: function () {
            this.$emit("deadline-changed", parseFloat(this.deadline));
            this.saved = true;
        },

        clearSaved: function () {
            this.saved = false;
        },

        updateSLA: function(value) {
            this.deadline = value;

            const plt = document.getElementById("slaPlot");
            try {
                Plotly.deleteTraces(plt, 1);

                const x = ["Longest query",
                           "Shortest query",
                           "All queries"];
                const y = [value, value, value];
                
                Plotly.addTraces(plt,
                                 [{x, y, type: "scatter", name: "SLA"}]);
            } catch (e) {
                // eat any errors (pre-init)
            }
        },

        redrawGraph: function () {
            const templateIDs = this.templates;

            const ourLatencies = [];
            for (let id in this.latencies) {
                let iid = parseInt(id);
                if (templateIDs.indexOf(iid) == -1)
                    continue;
                ourLatencies.push(this.latencies[id]);
            }
            
            
            const x = ["Longest query", "Shortest query", "All queries"];
            const y = [0, 0, 0];
            y[0] = _.max(ourLatencies)/1000;
            y[1] = _.min(ourLatencies)/1000;
            y[2] = _.sum(ourLatencies)/1000;

            this.minValue = y[0] + 5; // set the minimum slider value
            this.maxValue = y[2] * 2; // set the maximum slider value
            
            const layout = {
                autosize: false,
                width: 360,
                height: 300,
                margin: {
                    b: 70,
                    l: 40,
                    t: 10,
                    r: 40
                },
                yaxis: {
                    showgrid: true,
                    gridcolor: "#bdbdbd"
                }
            };

            const y2 = [this.deadline, this.deadline, this.deadline];
            Plotly.purge("slaPlot");
            Plotly.newPlot("slaPlot",
                           [{x, y, type: "bar", name: "Queries"},
                            {x, y: y2, type: "scatter", name: "SLA"}],
                           layout, {displayModeBar: false});
        }
    },

    watch: {
        templates: function(newTemplates) {
            this.redrawGraph();
            this.saved = false;
            this.$emit("deadline-changed", false);
        }
    },

    created: function () {
        this.getLatencyInfo();
    }    
};
