var _AAP = function() {
	console.log("Initializing AAP Media Monitoring Service...");
	this._ui = new _AAP_UI(this);
	this._VISIBLE_SOURCES = ["timesofindia.indiatimes.com", "www.hindustantimes.com", "www.indianexpress.com", "www.thehindu.com", "zeenews.india.com"];
	this._query = "";

	this._wordcloud_cache = {};
	this._articles_cache = {};
	this._breakdown_cache = {};
};

_AAP.prototype.init = function(config) {
	this._query = config.query;
	this.showArticleCountTrend(config.start, config.end);
	this.showArticles(config.start, config.end);
	this.showWordCloud(config.start, config.end);
};

_AAP.prototype.setQuery = function(query) {
	this._query = query;
}

_AAP.prototype.showAllTrendSeries = function() {
	this._ui.showAllTrendSeries();
};

_AAP.prototype.hideAllTrendSeries = function() {
	this._ui.hideAllTrendSeries();
};

_AAP.prototype.showArticlesForSrcDate = function(src, date) {
	console.log("Loading articles for query:" + this._query + " and source:" +  src + " and date:" + date);
	var params = {
		query : this._query,
		start : date,
		end   : date,
		src   : src
	};
	console.log(params);
	
	this._ui.showArticlesModalWithLoading(src, date);

	var that = this;

	// Update article list
	$.get('/articles/content', params, function(data, status, xhr) {
		if(data) {
			that._ui.hideArticlesModalLoading();
			that._ui.renderArticlesModal(data);
		}
	});
};

_AAP.prototype.updateContentForSrcDate = function(src, date) {
	console.log("Updating all UI components for query:" + this._query + " and source:" + src + " and date:" + date);
	this.showTrendBreakdown(date, date);
	this.showWordCloud(date, date, src);
	this.showArticles(date, date, src);
};

_AAP.prototype.showArticleCountTrend = function(start, end) {
	console.log("Loading Articles for query:" + this._query + " and start:" + start + " and end:" + end);

	this._ui.showTrendLoading();
	this._ui.showTrendBreakdownLoading();
	
	var params = {
		query : this._query,
		start : start,
		end   : end
	};
	var that = this;

	// Update trend graph
	$.get('/articles/count', params, function(data, status, xhr) {
		if(data) {
			var chart_data = {
				dates: [],
				series: []
			};

			for(var date in data.countByDate) {
				chart_data.dates.push(date);
			}

			var epochLatest = chart_data.dates[chart_data.dates.length-1];
			var breakdown_chart_data = {
				date: (new Date(parseInt(epochLatest))).toISOString().substr(0,10),
				series: []
			};		
			
			var count = 0;
			var colors = Highcharts.getOptions().colors;

			var total_data = new Array(chart_data.dates.length);

			for(var source in data.countBySrc) {
				var blob = {
					name: source,
					data: [],
					visible: (that._VISIBLE_SOURCES.indexOf(source) != -1)
				};
				for(var i=0; i<chart_data.dates.length; i++) {

					if(data.countBySrc[source][chart_data.dates[i]]) {
						blob.data.push([parseInt(chart_data.dates[i]), data.countBySrc[source][chart_data.dates[i]]]);
						total_data[i] = total_data[i] + data.countBySrc[source][chart_data.dates[i]] || data.countBySrc[source][chart_data.dates[i]];
					}
					else {
						blob.data.push([parseInt(chart_data.dates[i]), 0]);	
						total_data[i] = total_data[i] + 0 || 0;
					}

					if(chart_data.dates[i] == epochLatest && data.countBySrc[source][chart_data.dates[i]] > 0) {
						breakdown_chart_data.series.push({
							name: source,
							y: data.countBySrc[source][chart_data.dates[i]],
							color: colors[count++]
						})
					}
 
				}
				chart_data.series.push(blob);
			}

			var total_data_chart = [];
			for(var i=0; i<chart_data.dates.length; i++) {
				total_data_chart.push([parseInt(chart_data.dates[i]), total_data[i]]);
			}

			// All timeline
			chart_data.series.push({name: 'Total', data: total_data_chart});

			that._ui.hideTrendLoading();
			that._ui.renderArticleCountChart(chart_data);

			that._ui.hideTrendBreakdownLoading();
			that._ui.renderTrendBreakdownChart(breakdown_chart_data);


		}
	});
};

_AAP.prototype.showTrendBreakdown = function(start, end) {
	console.log("Loading Trend Breakdown for query:" + this._query + " and start:" + start + " and end:" + end);
	var params = {
		query : this._query,
		start : start,
		end   : end
	};
	var that = this;

	this._ui.showTrendBreakdownLoading();

	// Update article list
	$.get('/articles/count', params, function(data, status, xhr) {
		if(data) {

			var dates = [];

			for(var date in data.countByDate) {
				dates.push(date);
			}

			var epochLatest = dates[dates.length-1];

			var breakdown_chart_data = {
				date: (new Date(parseInt(epochLatest))).toISOString().substr(0,10),
				series: []
			};		
			
			var count = 0;
			var colors = Highcharts.getOptions().colors;

			for(var source in data.countBySrc) {
				for(var i=0; i<dates.length; i++) {

					if(dates[i] == epochLatest && data.countBySrc[source][dates[i]] > 0) {
						breakdown_chart_data.series.push({
							name: source,
							y: data.countBySrc[source][dates[i]],
							color: colors[count++]
						})
					}
 
				}
			}

			that._ui.renderTrendBreakdownChart(breakdown_chart_data);
			that._ui.hideTrendBreakdownLoading();

		}
	});	
};

_AAP.prototype.showArticles = function(start, end, src) {
	console.log("Loading Articles for query:" + this._query + " and start:" + start + " and end:" + end + " and src:" + src);
	var params = {
		query : this._query,
		start : start,
		end   : end,
		src   : src
	};
	var that = this;

	this._ui.showArticlesLoading();

	// Update article list
	$.get('/articles/content', params, function(data, status, xhr) {
		if(data) {
			that._ui.renderArticles(data);
			that._ui.hideArticlesLoading();
		}
	});
};

_AAP.prototype.showWordCloud = function(start, end, src) {
	console.log("Loading Word Cloud for query:" + this._query + " and start:" + start + " and end:" + end + " and src:" + src);
	this._ui.showWordCloudLoading();
	// Add loading code here
	
	var params = {
		query : this._query,
		start : start,
		end   : end,
		src   : src
	};
	var self = this;

	// Update article list
	$.get('/wordcloud', params, function(data, status, xhr) {
		if(data) {
			delete data[this._query];
			self._ui.renderWordCloud(data);
			self._ui.hideWordCloudLoading();
		}
	});

};

