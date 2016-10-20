const axios = require("axios");
const Plotly = require("plotly.js/lib/core");
Plotly.register([
    require('plotly.js/lib/bar')
]);
const deepcopy = require("deepcopy");

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
            minValue: 9,
            maxValue: 32,
            saved: false,
            graphColors: [],
            SLAType: 'Max'
        };
    },

    props: ['mode', 'templates'],
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
            if (this.deadline && this.mode) {
                $("html, body").animate({ scrollTop: $("#results").offset().top }, 900);
            }
        },

        clear: function () {
            this.saved = false;
            this.$emit("deadline-changed", false);
        },

        updateSLA: function(value) {
            this.deadline = value;
            var x = this.x;
            var y = this.y;

            const plt = document.getElementById("slaPlot");
            try {
                Plotly.deleteTraces(plt, 30);

                const y = x.map(x => this.deadline);

                Plotly.addTraces(plt,
                                 [{x, y, type: "scatter", name: "SLA"}]);
            } catch (e) {
                // eat any errors (pre-init)
            }
        },

        redrawGraph: function () {
           if (this.templates.length == 0 || this.latencies.length == 0)
                return;
           this.getXY();
           var x = this.x;
           var y = this.y;

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
                   title: "Latency (s)",
                   showgrid: true,
                   gridcolor: "#bdbdbd",
                   range: [0, 36]
               },
               xaxis: {
                   title: "Query Template",
               },
               title: "SLA on Selected Query Templates",
               barmode: 'stack',
               showlegend: false,
               hoverinfo: 'none'
           };

           var colors = this.graphColors;
           let trace = {
               x, y,
               type: "bar",
               name: "Latency (s)",
               orientation: 'v',
               hoverinfo: 'none',
               marker: {
                   color: colors,
                   line: {
                      color: '#fff',
                      width: 3
                    }
               }
           };
           let trace2 = {
               x, y,
               type: "bar",
               name: "",
               orientation: 'v',
               opacity: .2,
               hoverinfo: 'none',
               marker: {
                   color: colors,
                   line: {
                      color: '#fff',
                      width: 3
                    }
               }
           };

           const y2 = x.map(x => this.deadline);
           var traceHack = [trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2]

           Plotly.purge("slaPlot");
           Plotly.newPlot("slaPlot",
                          [trace, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2, trace2,  {x, y: y2, type: "scatter", name: "SLA"}],
                          layout, {displayModeBar: false});
       },

       getXY: function() {
           const templateIDs = deepcopy(this.templates);

           const ourLatencies = [];
           for (let id in this.latencies) {
               let iid = parseInt(id);
               if (templateIDs.indexOf(iid) == -1)
                   continue;
               ourLatencies.push(this.latencies[id]);
           }

           templateIDs.sort();
           this.x = templateIDs.map(x => "Q" + x);
           this.y = templateIDs.map(x => this.latencies[x]/1000);

           for (let i = 0; i < this.templates.length; i++) {
               this.graphColors.push("rgb(206,206,206,1)");
           }

           var colors = ["FBD1A2", "#F79256", "#7DCFB6", "#00B2CA", "#987284"];
           for (var i = 0; i < templateIDs.length; i++) {
               this.graphColors[i] = colors[templateIDs[i]-1];
           }
       }
    },

    watch: {
        templates: function(newTemplates) {
            this.redrawGraph();
            this.saved = false;
            this.$emit("deadline-changed", false);
        },
        SLAType: function() {
            this.redrawGraph();
            this.saved = false;
            this.$emit("deadline-changed", false);
        }
    },

    created: function () {
        this.getLatencyInfo();
    }
};
