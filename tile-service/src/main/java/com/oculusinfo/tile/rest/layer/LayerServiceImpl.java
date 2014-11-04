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
package com.oculusinfo.tile.rest.layer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.oculusinfo.binning.TileIndex;
import com.oculusinfo.binning.io.PyramidIO;
import com.oculusinfo.binning.io.PyramidIOFactory;
import com.oculusinfo.binning.metadata.PyramidMetaData;
import com.oculusinfo.binning.util.JsonUtilities;
import com.oculusinfo.factory.ConfigurableFactory;
import com.oculusinfo.factory.ConfigurationException;
import com.oculusinfo.factory.EmptyConfigurableFactory;
import com.oculusinfo.tile.init.FactoryProvider;
import com.oculusinfo.tile.init.providers.CachingLayerConfigurationProvider;
import com.oculusinfo.tile.rendering.LayerConfiguration;
import com.oculusinfo.tile.rest.RequestParamsFactory;
import com.oculusinfo.tile.rest.tile.caching.CachingPyramidIO.LayerDataChangedListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.*;

@Singleton
public class LayerServiceImpl implements LayerService {
	private static final Logger LOGGER = LoggerFactory.getLogger(LayerServiceImpl.class);

	private List< JSONObject > _layers;
	private Map< String, JSONObject > _layersById;
	private Map< String, JSONObject > _metaDataCache;
    private FactoryProvider<LayerConfiguration> _layerConfigurationProvider;


	@Inject
	public LayerServiceImpl (@Named("com.oculusinfo.tile.layer.config") String layerConfigurationLocation,
	                         FactoryProvider<LayerConfiguration> layerConfigurationProvider) {
		_layers = new ArrayList<>();
		_layersById = new HashMap<>();
		_metaDataCache = new HashMap<>();
        _layerConfigurationProvider = layerConfigurationProvider;

		if (layerConfigurationProvider instanceof CachingLayerConfigurationProvider) {
			((CachingLayerConfigurationProvider) layerConfigurationProvider).addLayerListener(new LayerDataChangedListener () {
					public void onLayerDataChanged (String layerId) {
						_metaDataCache.remove(layerId);
					}
				});
		}

		readConfigFiles( getConfigurationFiles( layerConfigurationLocation ) );
	}

	@Override
	public List< JSONObject > getLayerConfigs() {
		return _layers;
	}

	@Override
	public PyramidMetaData getMetaData( String layerId ) {
		try {
			LayerConfiguration config = getLayerConfiguration( layerId, null, null );
            String dataId = config.getPropertyValue(LayerConfiguration.DATA_ID);
            System.out.println( dataId );
			PyramidIO pyramidIO = config.produce( PyramidIO.class );
			return getMetaData( layerId, dataId, pyramidIO );
		} catch (ConfigurationException e) {
			LOGGER.error( "Couldn't determine pyramid I/O method for {}", layerId, e );
			return null;
		}
	}
    
	private PyramidMetaData getMetaData( String layerId, String dataId, PyramidIO pyramidIO ) {
		try {
			JSONObject metadata = _metaDataCache.get( layerId );
			if ( metadata == null ) {
                System.out.println( "Checking for meta data for: " +  layerId );
				String s = pyramidIO.readMetaData( dataId );
				if ( s == null ) {
					throw new JSONException( "Missing metadata." );
				}
				metadata = new JSONObject( s );
				_metaDataCache.put( layerId, metadata );
			}
			return new PyramidMetaData( metadata );
		} catch (JSONException e) {
			LOGGER.error("Metadata file for layer is missing or corrupt: {}", layerId, e);
		} catch (IOException e) {
			LOGGER.error("Couldn't read metadata: {}", layerId, e);
		}
		return null;
	}

	/**
	 * Wraps the options and query {@link JSONObject}s together into a new object.
	 */
	private JSONObject mergeQueryConfigOptions(JSONObject options, JSONObject query) {
        JSONObject result = JsonUtilities.deepClone( options );
		try {
            if (query != null) {
                Iterator<?> keys = query.keys();
                while ( keys.hasNext() ) {
                    String key = (String) keys.next();
                    result.put( key, query.get( key ) );
                }
            }
		}
		catch (Exception e) {
			LOGGER.error("Couldn't merge query options with main options.", e);
		}
		return result;
	}
    /*
    private JSONObject mergeQueryConfigOptions(JSONObject options, JSONObject query) {
        JSONObject result = new JSONObject();
		try {
            JSONObject config = JsonUtilities.deepClone( options );
            if (query != null) {
                Iterator<?> keys = query.keys();
                while ( keys.hasNext() ) {
                    String key = (String) keys.next();
                    System.out.println( key );
                    config.put( key, query.get( key ) );
                }
            }
            result.put( "config", config );
		}
		catch (Exception e) {
			LOGGER.error("Couldn't merge query options with main options.", e);
		}
		return result;
	}
     */
    /*
    private JSONObject mergeQueryConfigOptions(JSONObject options, JSONObject query) {
        JSONObject result = new JSONObject();
		try {
            JSONObject config = JsonUtilities.deepClone( options );
            if (query != null) {
                config.put( "renderer", query );
            }
            result.put( "config", config );
		}
		catch (Exception e) {
			LOGGER.error("Couldn't merge query options with main options.", e);
		}
		return result;
	}
	*/

    @Override
	public LayerConfiguration getLayerConfiguration( String layerId, TileIndex tile, JSONObject requestParams ) {
		try {

            JSONObject layerConfig = _layersById.get( layerId );

			//the root factory that does nothing
			EmptyConfigurableFactory rootFactory = new EmptyConfigurableFactory(null, null, null);
			
			//add another factory that will handle query params
			RequestParamsFactory queryParamsFactory = new RequestParamsFactory(null, rootFactory, Collections.<String>emptyList());
			rootFactory.addChildFactory(queryParamsFactory);
			
			//add the layer configuration factory under the path 'config'
			ConfigurableFactory<LayerConfiguration> factory = _layerConfigurationProvider.createFactory( rootFactory, new ArrayList<String>() );
			rootFactory.addChildFactory(factory);

			rootFactory.readConfiguration( mergeQueryConfigOptions( layerConfig, requestParams ) );

			LayerConfiguration config = rootFactory.produce( LayerConfiguration.class );

			// Initialize the PyramidIO for reading
			String dataId = config.getPropertyValue(LayerConfiguration.DATA_ID);
            PyramidIO pyramidIO = config.produce( PyramidIO.class );
			JSONObject initJSON = config.getProducer( PyramidIO.class ).getPropertyValue( PyramidIOFactory.INITIALIZATION_DATA );

            if ( initJSON != null ) {
				int width = config.getPropertyValue(LayerConfiguration.OUTPUT_WIDTH);
				int height = config.getPropertyValue(LayerConfiguration.OUTPUT_HEIGHT);
				Properties initProps = JsonUtilities.jsonObjToProperties(initJSON);
                System.out.println( "Initializing pyramid for read" );
				pyramidIO.initializeForRead( dataId, width, height, initProps);
			}

            if ( tile != null ) {
                PyramidMetaData metadata = getMetaData( layerId, dataId, pyramidIO );
                String minimum = metadata.getCustomMetaData(""+tile.getLevel(), "minimum");
                String maximum = metadata.getCustomMetaData(""+tile.getLevel(), "maximum");
                config.setLevelProperties(tile, minimum, maximum);
            }

			return config;

		} catch ( Exception e ) {
			LOGGER.warn("Error configuring rendering for", e);
			return null;
		}
	}



	// ////////////////////////////////////////////////////////////////////////
	// Section: Configuration reading methods
	//
	private File[] getConfigurationFiles (String location) {
		try {
			// Find our configuration file.
			URI path = null;
			if (location.startsWith("res://")) {
				location = location.substring(6);
				path = LayerServiceImpl.class.getResource(location).toURI();
			} else {
				path = new File(location).toURI();
			}

			File configRoot = new File(path);
			if (!configRoot.exists())
				throw new Exception(location+" doesn't exist");

			if (configRoot.isDirectory()) {
				return configRoot.listFiles();
			} else {
				return new File[] {configRoot};
			}
		} catch (Exception e) {
			LOGGER.warn("Can't find configuration file {}", location, e);
			return new File[0];
		}
	}

	private void readConfigFiles( File[] files ) {
		for (File file: files) {
			try {
				JSONObject contents = new JSONObject(new JSONTokener(new FileReader(file)));
                Iterator<?> keys = contents.keys();

                while( keys.hasNext() ){
                    String key = (String)keys.next();
                    if( contents.get(key) instanceof JSONObject ) {
                        JSONObject layerJSON = contents.getJSONObject(key);
                        layerJSON.put("layer", key ); // append layer name
                        _layersById.put( key, layerJSON );
                        _layers.add( layerJSON );
                        System.out.println( "key: " + key );
                        System.out.println( layerJSON.toString( 4 ) );
                    }
                }

			} catch (FileNotFoundException e) {
				LOGGER.error("Cannot find layer configuration file {} ", file, e);
				return;
			} catch (JSONException e) {
				LOGGER.error("Layer configuration file {} was not valid JSON.", file, e);
			}
		}
	}
}
