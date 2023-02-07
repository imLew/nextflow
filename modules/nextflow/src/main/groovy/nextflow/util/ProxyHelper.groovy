/*
 * Copyright 2023, Seqera Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package nextflow.util

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * Helper methods for proxy configuration.
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 * @author Ben Sherman <bentshermann@gmail.com>
 */
@Slf4j
@CompileStatic
class ProxyHelper {

    /**
     * Set up environment and system properties. It checks the following
     * environment variables:
     * - http_proxy
     * - https_proxy
     * - ftp_proxy
     * - HTTP_PROXY
     * - HTTPS_PROXY
     * - FTP_PROXY
     * - NO_PROXY
     */
    static void setupEnvironment() {

        setProxy('HTTP', System.getenv())
        setProxy('HTTPS', System.getenv())
        setProxy('FTP', System.getenv())

        setProxy('http', System.getenv())
        setProxy('https', System.getenv())
        setProxy('ftp', System.getenv())

        setNoProxy(System.getenv())
    }

    /**
     * Set no proxy property if defined in the launching env
     *
     * See:
     * https://docs.oracle.com/javase/8/docs/technotes/guides/net/proxies.html
     *
     * @param env
     */
    static void setNoProxy(Map<String,String> env) {
        final noProxy = env.get('NO_PROXY') ?: env.get('no_proxy')
        if(noProxy) {
            System.setProperty('http.nonProxyHosts', noProxy.tokenize(',').join('|'))
        }
    }

    /**
     * Setup proxy system properties and optionally configure the network authenticator
     *
     * See:
     * http://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html
     * https://github.com/nextflow-io/nextflow/issues/24
     *
     * @param qualifier Either {@code http/HTTP} or {@code https/HTTPS}.
     * @param env The environment variables system map
     */
    static void setProxy(String qualifier, Map<String,String> env ) {
        assert qualifier in ['http','https','ftp','HTTP','HTTPS','FTP']
        def str = null
        def var = "${qualifier}_" + (qualifier.isLowerCase() ? 'proxy' : 'PROXY')

        // -- setup HTTP proxy
        try {
            final proxy = ProxyConfig.parse(str = env.get(var.toString()))
            if( proxy ) {
                // set the expected protocol
                proxy.protocol = qualifier.toLowerCase()
                log.debug "Setting $qualifier proxy: $proxy"
                System.setProperty("${qualifier.toLowerCase()}.proxyHost", proxy.host)
                if( proxy.port )
                    System.setProperty("${qualifier.toLowerCase()}.proxyPort", proxy.port)
                if( proxy.authenticator() ) {
                    log.debug "Setting $qualifier proxy authenticator"
                    Authenticator.setDefault(proxy.authenticator())
                }
            }
        }
        catch ( MalformedURLException e ) {
            log.warn "Not a valid $qualifier proxy: '$str' -- Check the value of variable `$var` in your environment"
        }

    }

}