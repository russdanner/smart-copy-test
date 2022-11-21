import org.craftercms.search.elasticsearch.ElasticsearchWrapper
import org.craftercms.engine.service.context.SiteContext
import java.util.regex.Pattern

def baseUrl = (pluginConfig.getString("baseUrl")) ? pluginConfig.getString("baseUrl") : "ConfigureThisPlugin"
baseUrl+="/abc/"
// Go to search and get all pages that are NOT disabled
def pageItems = queryPages(params.route, elasticsearch)

// get black list patterns
def excludePatterns = getBlackListPatterns(siteConfig)

// Prepare the response
def pages = []

pageItems.each { v ->
    def issue = ""
    // check if the current item is black listed
    def blackListed = false
    excludePatterns.any { patternAsStr ->
        def excludePattern = Pattern.compile(patternAsStr, Pattern.CASE_INSENSITIVE)
        def excludeMatcher = excludePattern.matcher( v.localId )
        blackListed = excludeMatcher.find()

        // don't keep searching patterns. A match was found
        if(blackListed) return true
    }

    // if not black listed, add it        
    if(blackListed == false) {
        def urlItem = [:]
        SiteContext context = SiteContext.current 
        
        def url = urlTransformationService.transform("storeUrlToRenderUrl", v.localId)
        def fullyQualifiedUrl = concatUrlParts(baseUrl, url)
        
        urlItem.loc = fullyQualifiedUrl
        urlItem.lastmod = v.lastModifiedDate_dt  
        urlItem.changefreq = "weekly" 
        urlItem.priority = "0.8000"

        pages.add(urlItem)
    }
}


templateModel.pages = pages

response.setContentType("application/xml")

return "/templates/plugins/org/rd/plugin/sitemapgenerator/sitemapgenerator/sitemap.ftl"

/**
 * get site map blacklist 
 */ 
def getBlackListPatterns(siteConfig) {
    
    // This has been left in to facilitate testing
    // return ["^.*level[\\.]xml\$",            // level descriptors
    //         "/site/website/r/.*",             // exclude redirects
    //         "/site/website/resources/lp/.*",  // parameterized landing pages
    //         "/site/website/resources/cp/.*",  // protected content pages
    //         "/site/website/resources/vp/.*"   // protected video pages
    // ]

    // The engine config provides configurable black list
    // This can be used to stop entire classes of URLs from showing on the sitemap
    def excludePatterns = []
    def excludePatternsConfig = siteConfig.getStringArray("siteMap.excludes")
    
    if(!excludePatternsConfig) {
        // if no patterns specified, are excluded assume 
        // that no level descriptors is the default black list
        excludePatterns.add("^.*level[\\.]xml\$")    
    }
    else {
        excludePatterns = excludePatternsConfig
    }
    
    return excludePatterns
}

/**
 * query pages for a given route
 */
def queryPages(route, elasticsearch) {
    def routeFolder = params.route ? params.route : "/"
    def path = '/site/website'+routeFolder+'*'
    def results = elasticsearch.search( 
        [   from: 1, size:1000,
            query: 
            [ bool: 
                [ filter: 
                    [ wildcard: [ 'localId': path ] ] 
                    ] 
                ] 
        ]).hits.hits*.sourceAsMap  
    return results  
}

/**
 * concat serverName and URL and account for trailing and leading slashes
 */
def concatUrlParts(serverName, url){
    def server = serverName.endsWith("/") ? serverName.substring(0, serverName.length()-1) : serverName
    return server + url
}
