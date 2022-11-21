<?xml version="1.0" encoding="utf-8" standalone="yes"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
  <#list pages as page>
      <url>
        <loc>${page.loc}</loc>
        <#if page.lastmod?has_content>
        <lastmod>${page.lastmod}</lastmod>
        </#if>
        <#if page.changefreq?has_content>
        <changefreq>${page.changefreq}</changefreq>
        </#if>
        <#if page.priority?has_content>
        <priority>${page.priority}</priority>
        </#if>
      </url>
  </#list>
</urlset>