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

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

(function() {

    "use strict";

    var Layer = require('./Layer'),
        PubSub = require('../util/PubSub');

    /**
     * Instantiate an ElasticLayer object.
     * @class ElasticLayer
     * @augments Layer
     * @classdesc A client rendered layer object. Uses ElasticSearch queries to draw
     * geospatial data on a map.
     *
     * @param {Object} spec - The specification object.
     * <pre>
     * {
     *     opacity  {float}    - The opacity of the layer. Default = 1.0
     *     enabled  {boolean}  - Whether the layer is visible or not. Default = true
     *     zIndex   {integer}  - The z index of the layer. Default = 1000
     *     renderer {Renderer} - The tile renderer object. (optional)
     *     html {String|Function|HTMLElement|jQuery} - The html for the tile. (optional)
     * }
     * </pre>
     */
    function ElasticLayer(spec) {
        // call base constructor
        Layer.call(this, spec);
        // set reasonable defaults
        this.zIndex = (spec.zIndex !== undefined) ? spec.zIndex : 749;
        this.domain = "elastic";

        this.source = spec.source;
        this.styleMap = spec.styleMap;
        this.eventListeners = spec.eventListeners;
        this.strategies = spec.strategies;
        this.vectors = spec.vectors;
    }

    ElasticLayer.prototype = Object.create(Layer.prototype);

    /**
     * Activates the layer object. This should never be called manually.
     * @memberof ElasticLayer
     * @private
     */
    ElasticLayer.prototype.activate = function() {

        var layerSpec = {};
        if (this.strategies) {
        	layerSpec.strategies = this.strategies;
        }
        if (this.styleMap) {
        	layerSpec.styleMap = this.styleMap;
        }
        if (this.eventListeners) {
        	layerSpec.eventListeners = this.eventListeners;
        }

        this.olLayer = new OpenLayers.Layer.Vector("Overlay", layerSpec);

        this.map.olMap.addLayer(this.olLayer);

        this.olLayer.addFeatures(this.vectors);

        this.setZIndex(this.zIndex);
        this.setOpacity(this.opacity);
        this.setEnabled(this.enabled);
        this.setTheme(this.map.getTheme());

    };

    /**
     * Dectivates the layer object. This should never be called manually.
     * @memberof ClientLayer
     * @private
     */
    ElasticLayer.prototype.deactivate = function() {
        if (this.olLayer) {
            this.map.olMap.removeLayer(this.olLayer);
            this.olLayer.destroyFeatures();
            this.olLayer.destroy();
        }
    };

    /**
     * Updates the theme associated with the layer.
     * @memberof ClientLayer
     *
     * @param {String} theme - The theme identifier string.
     */
    ElasticLayer.prototype.setTheme = function(theme) {
        this.theme = theme;
    };

    /**
     * Get the current theme for the layer.
     * @memberof ClientLayer
     *
     * @returns {String} The theme identifier string.
     */
    ElasticLayer.prototype.getTheme = function() {
        return this.theme;
    };

    /**
     * Set the z index of the layer.
     * @memberof ClientLayer
     *
     * @param {integer} zIndex - The new z-order value of the layer, where 0 is front.
     */
    ElasticLayer.prototype.setZIndex = function(zIndex) {
        // we by-pass the OpenLayers.Map.setLayerIndex() method and manually
        // set the z-index of the layer dev. setLayerIndex sets a relative
        // index based on current map layers, which then sets a z-index. This
        // caused issues with async layer loading.
        this.zIndex = zIndex;
        if (this.olLayer) {
            $(this.olLayer.div).css('z-index', zIndex);
            PubSub.publish(this.getChannel(), {
                field: 'zIndex',
                value: zIndex
            });
        }

    };

    /**
     * Get the layers zIndex.
     * @memberof ClientLayer
     *
     * @returns {integer} The zIndex for the layer.
     */
    ElasticLayer.prototype.getZIndex = function() {
        return this.zIndex;
    };

    module.exports = ElasticLayer;
}());
