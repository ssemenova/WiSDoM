const axios = require("axios");
const deepcopy = require("deepcopy");
const Plotly = require("plotly.js/lib/core");
Plotly.register([
    require('plotly.js/lib/bar')
]);

module.exports = {
    data: function () {
        return {
            templates: [],
            selected: [],
            latencies: [],
            saved: false
        };
    },

    methods: {
        selectedTemplates: function (){
            return this.selected.map(x => parseInt(x));
        },
        numSelected: function () {
            return this.selected.length;
        },
        getTemplates: function () {
            axios.get("/querytemplates")
                .then((r) => {
                    this.templates = r.data;
                });

            axios.get("/querylatency")
                .then(r => {
                    this.latencies = r.data;
                });
        },

        save: function () {
            this.saved = true;
            this.$emit("templates-changed", deepcopy(this.selectedTemplates()));
        },

        clear: function() {
            this.saved = false;
        },

        redrawGraph: function () {
            if (this.templates.length == 0 || this.latencies.length == 0)
                return;

            const y = this.templates.map(x => "Q" + x.id);
            const x = this.templates.map(x => this.latencies[x.id]/1000);
            x.reverse();
            y.reverse();

            const layout = {
                autosize: false,
                width: 450,
                height: 350,
                margin: {
                    b: 40,
                    l: 40,
                    t: 40,
                    r: 40
                },
                yaxis: {
                    showgrid: true,
                    gridcolor: "#bdbdbd"
                },
                xaxis: {
                    title: "Latency (s)"
                },
                title: "Selected Templates"
            };

            let trace = {
                x, y,
                type: "bar",
                name: "Queries",
                orientation: 'h',
                marker: {
                    color: []
                }
            };

            for (let i = 0; i < this.templates.length; i++) {
                trace.marker.color.push("rgb(206,206,206,1)");
            }

            var colors = ["E8D6CB", "#F79256", "#FBD1A2", "#7DCFB6", "#00B2CA"];
            this.selected.forEach(function(x) {
                trace.marker.color[trace.marker.color.length - parseInt(x)] = colors[x-1];
            });

            Plotly.purge("latencyPlot");
            Plotly.newPlot("latencyPlot",
                           [trace],
                           layout, {displayModeBar: false});
        }

    },

    watch: {
        templates: function() { this.redrawGraph(); },
        latencies: function() { this.redrawGraph(); },
        selected: function() { this.redrawGraph(); }
    },

    created: function () {
        this.getTemplates();
    }
};
