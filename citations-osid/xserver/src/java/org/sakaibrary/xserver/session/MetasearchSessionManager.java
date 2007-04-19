package org.sakaibrary.xserver.session;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * MetasearchSessionManager is a Singleton class designed for session
 * management in metasearching applications.  It makes use of ehcache
 * and MetasearchSession objects indexed by globally unique identifiers
 * to hold all session state for individual sessions.
 * 
 * @author gbhatnag
 */
public class MetasearchSessionManager implements java.io.Serializable {
  /* constants */
  private static final String CACHE_NAME = "org.sakaibrary.xserver.session.MetasearchSession";
  private static final org.apache.commons.logging.Log LOG =
		org.apache.commons.logging.LogFactory.getLog(
				"org.sakaibrary.osid.repository.xserver.session.MetasearchSessionManager" );

  /* private static variables */
  private static MetasearchSessionManager metasearchSessionManager;
  private static CacheManager cacheManager;
  private static Cache cache;

  /**
   * Private constructor to ensure only one MetasearchSessionManager
   * is instantiated.  Initializes ehcache components CacheManager
   * and Cache.
   */
  private MetasearchSessionManager() {
    try {
      cacheManager = CacheManager.create();

      // add the cache to the CacheManager if it doesn't already exist
      if( !cacheManager.cacheExists( CACHE_NAME ) ) {
        // create a cache using ehcache 1.2.4 constructor
        Cache temp = new Cache( 
        	CACHE_NAME,    // cache name
            50,            // maxElementsInMemory
            net.sf.ehcache.store.MemoryStoreEvictionPolicy.LRU,
            true,          // overflowToDisk
            "ignored",     // disk store path - ignored, CacheManager sets it using setter injection
            false,         // eternal
            0L,            // time to live (seconds)
            900L,          // time to idle (seconds)
            false,         // diskPersistent
            120L,          // diskExpiryThreadIntervalSeconds
            null,          // registeredEventListeners
            null           // bootstrapCacheLoader
        );
        cacheManager.addCache( temp );
      }

      // get cache for use
      cache = cacheManager.getCache( CACHE_NAME );
    } catch( CacheException ce ) {
      LOG.warn( "MetasearchSessionManager() failed to create CacheManager or Cache", ce );
    }

    LOG.info( "ehcache session initiated properly." );
  }

    /**
     * Gets the Singleton instance of MetasearchSessionManager
     * 
     * @return an instance of MetasearchSessionManager
     */
    public static synchronized MetasearchSessionManager getInstance() {
      if( metasearchSessionManager == null ) {
        metasearchSessionManager = new MetasearchSessionManager();
      }

      return metasearchSessionManager;
    }

    /**
     * Puts the MetasearchSession object into the MetasearchSessionManager
     * cache indexed by the guid.  If the guid already exists, the
     * MetasearchSession object is updated with the given object.
     * 
     * @param guid a globally unique identifier String
     * @param ms the MetasearchSession object to be put/updated in the
     * MetasearchSessionManager cache.
     */
    public void putMetasearchSession( String guid,
        MetasearchSession ms ) {
      // given guid and ms.getGuid() should match -- TODO new Exception Type?
      if( !ms.getGuid().equals( guid ) ) {
        LOG.warn( "putMetasearchSession(): putting MetasearchSession into " +
            "ehcache with mismatched guids..." );
      }

      // the following puts if guid is new, updates if guid is old
      cache.put( new Element( guid, ms ) );
    }

    /**
     * Gets the MetasearchSession object out of the MetasearchSessionManager
     * cache indexed by the guid.
     * 
     * @param guid a globally unique identifier String
     * @return the MetasearchSession object if it exists and has not expired,
     * otherwise, null
     */
    public MetasearchSession getMetasearchSession( String guid ) {
      Element element = null;
      try {
        element = cache.get( guid );
      } catch( CacheException ce ) {
        LOG.warn( "MetasearchSessionManager.getMetasearchSession()" +
            " cannot get cache with guid: " + guid, ce );
      }
      
      // element could have expired
      boolean isExpired = ( element == null ) ? true : cache.isExpired( element );
    
      return isExpired ? null : ( MetasearchSession )element.getValue();
    }
  }