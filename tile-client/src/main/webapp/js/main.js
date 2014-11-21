/*
 * Copyright (c) 2014 Oculus Info Inc.
 * http://www.oculusinfo.com/
 * 
 * Released under the MIT License.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

require(['./util/Util',
         './map/Map',
         './layer/LayerService',
         './layer/base/BaseLayer',
         './layer/server/ServerLayer',
         //'./layer/client/ClientLayer',
        './layer/client/HtmlTileLayer'],
         //'./layer/client/renderers/HtmlRenderer'],

        function( Util,
                  Map,
                  LayerService,
                  BaseLayer,
                  ServerLayer,
                  //ClientLayer,
                  HtmlTileLayer
                  //HtmlRenderer
                  ) {

	        "use strict";

	        var layerConfigFile = "data/layer-config.json",
	            mapConfigFile = "data/map-config.json",
	            viewConfigFile = "data/view-config.json",
	            layerDeferred,
	            mapDeferred,
	            viewDeferred,
	            map;


            /**
             * GET a configuration file from server and resolve the returned
             * $.Deferred when it is received.
             */
	        function getConfigFile( file ) {
	            var deferred = $.Deferred();
	            $.getJSON( file )
                    .done( function( config ) {
                        deferred.resolve( config );
                    })
                    .fail( function( jqxhr, textStatus, error ) {
                        var err = textStatus + ", " + error;
                        console.log( "Request to GET "+file+" failed: " + err );
                    });
                return deferred;
	        }

            /**
             * Assemble current view from view configuration. If there are multiple view,
             * create the corresponding overlay button.
             */
	        function assembleView( layerConfig, mapConfig, viewConfig ) {

                var currentView,
                    map,
                    layers,
                    i;

                currentView = viewConfig[ 0 ];

                layers = [];
                for ( i=0; i<currentView.layers.length; i++ ) {
                    layers.push( layerConfig[ currentView.layers[i] ] );
                }
                map = mapConfig[ currentView.map ];

                return {
                    layers: layers,
                    map: map
                };
	        }

            /*
             * GET all client-side configuration files
             */
            layerDeferred = getConfigFile( layerConfigFile );
            mapDeferred = getConfigFile( mapConfigFile );
            viewDeferred = getConfigFile( viewConfigFile );

            $.when( layerDeferred,
                    mapDeferred,
                    viewDeferred ).done( function( layerConfig, mapConfig, viewConfig ) {

                var layersDeferred = $.Deferred();

		        // Get our list of layers
		        LayerService.requestLayers( function( layers ) {
		            layersDeferred.resolve( layers );
		        });

		        $.when( layersDeferred ).done( function ( layers ) {
				        var view,
				            baseLayer,
                            //clientLayer,
                            htmlLayer,
				            serverLayer;

				        view = assembleView( layerConfig, mapConfig, viewConfig );

                        baseLayer = new BaseLayer( {
                            "type": "Google",
                            "theme" : "dark",
                            "options" : {
                                "name" : "Dark",
                                "type" : "styled",
                                "style" : [
                                    {
                                        'featureType': 'all',
                                        'stylers': [
                                            { 'saturation': -100 },
                                            { 'invert_lightness' : true },
                                            { 'visibility' : 'simplified' }
                                        ]
                                    },
                                    {
                                        'featureType': 'landscape.natural',
                                        'stylers': [
                                            { 'lightness': -50 }
                                        ]
                                    },
                                    {
                                        'featureType': 'poi',
                                        'stylers': [
                                            { 'visibility': 'off' }
                                        ]
                                    },
                                    {
                                        'featureType': 'road',
                                        'stylers': [
                                            { 'lightness': -50 }
                                        ]
                                    },
                                    {
                                        'featureType': 'road',
                                        'elementType': 'labels',
                                        'stylers': [
                                            { 'visibility': 'off' }
                                        ]
                                    },
                                    {
                                        'featureType': 'road.highway',
                                        'stylers': [
                                            { 'lightness': -60 }
                                        ]
                                    },
                                    {
                                        'featureType': 'road.arterial',
                                        'stylers': [
                                            { 'visibility': 'off' }
                                        ]
                                    },
                                    {
                                        'featureType': 'administrative',
                                        'stylers': [
                                            { 'lightness': 10 }
                                        ]
                                    },
                                    {
                                        'featureType': 'administrative.province',
                                        'elementType': 'geometry',
                                        'stylers': [
                                            { 'lightness': 15 }
                                        ]
                                    },
                                    {
                                        'featureType' : 'administrative.country',
                                        'elementType' : 'geometry',
                                        'stylers' : [
                                            { 'visibility' : 'on' },
                                            { 'lightness' : -56 }
                                        ]
                                    },
                                    {
                                        'elementType' : 'labels',
                                        'stylers' : [
                                            { 'lightness' : -46 },
                                            { 'visibility' : 'on' }
                                        ] }
                                ]
                            }
                        });

                        serverLayer = new ServerLayer({
                            source: layers["tweet-heatmap"],
                            valueTransform: {
                                type: "log10"
                            }
                        });

                        /*
                        clientLayer = new ClientLayer({
                            views: [
                                {
                                    source: layers["top-tweets"],
                                    renderer: new HtmlRenderer({
                                        html: '<div style="position:relative; left: 100px; top: 100px; width:56px; height:56px; background-color:blue;"/>'
                                    })
                                }
                            ]
                        });
                        */

                        htmlLayer = new OpenLayers.Layer.Html(
                            "Html test",
                            layers["top-tweets"].tms,
                            {
                                layername: layers["top-tweets"].id,
                                type: 'json',
                                maxExtent: new OpenLayers.Bounds(-20037500, -20037500,
                                                                  20037500,  20037500),
                                getURL: function createUrl( bounds ) {
                                    var res = this.map.getResolution(),
                                        maxBounds = this.maxExtent,
                                        tileSize = this.tileSize,
                                        x = Math.round( (bounds.left-maxBounds.left) / (res*tileSize.w) ),
                                        y = Math.round( (bounds.bottom-maxBounds.bottom) / (res*tileSize.h) ),
                                        z = this.map.getZoom(),
                                        fullUrl;
                                    if (x >= 0 && y >= 0) {
                                        // set base url
                                        fullUrl = ( this.url + this.layername + "/" +
                                                   z + "/" + x + "/" + y + "." + this.type);
                                        return fullUrl;
                                    }
                                },
                                isBaseLayer: false,
                                html: function( data )  {
                                    if ( data.tile ) {
                                        return '<div style="position:relative; left: 100px; top: 100px; width:56px; height:56px; background-color:blue;"/>';
                                    }
                                    return '';
                                }
                            }
                        );

                        view.map.id = "map";
                        map = new Map( view.map );
                        map.add( baseLayer );
                        map.add( serverLayer );
                        //map.add( clientLayer );
                        map.map.addLayer( htmlLayer );
			        }
		        );
	        }, 'json');
        });
