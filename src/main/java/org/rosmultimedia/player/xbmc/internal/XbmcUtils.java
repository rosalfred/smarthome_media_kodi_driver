/**
 * This file is part of the Alfred package.
 *
 * (c) Mickael Gaillard <mick.gaillard@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package org.rosmultimedia.player.xbmc.internal;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.google.common.base.Strings;

/**
 *
 * @author Erwan Le Huitouze <erwan.lehuitouze@gmail.com>
 *
 */
public class XbmcUtils {
    /**
     * Convert image url from xbmc json-rpc
     */
    public static String getImageUrl(String url) {
        String imageurl = url;

        if (!Strings.isNullOrEmpty(imageurl)) {
            if (imageurl.contains("http")) {
                try {
                    imageurl = URLDecoder.decode(
                            imageurl.replace("image://", ""), "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            /*String credentials = "";

            if (!Strings.isNullOrEmpty(this.node.getUser())) {
                credentials += this.node.getUser();
            }

            if (!Strings.isNullOrEmpty(this.node.getPassword())) {
                credentials += ":" + this.node.getPassword();
            }

            if (!Strings.isNullOrEmpty(credentials)) {
                credentials += "@";
            }

            imageurl = String.format("http://%s%s:%s/", credentials,
                    this.node.getHost(), this.node.getPort())
                    + imageurl;

            imageurl = imageurl.replace("image://", "image/");*/

            if (imageurl.endsWith("/")) {
                imageurl = imageurl.substring(0, imageurl.length() - 1);
            }
        }

        return imageurl;
    }
}
