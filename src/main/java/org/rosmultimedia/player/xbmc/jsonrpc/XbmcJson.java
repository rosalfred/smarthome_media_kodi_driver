/**
 * This file is part of the Alfred package.
 *
 * (c) Mickael Gaillard <mick.gaillard@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package org.rosmultimedia.player.xbmc.jsonrpc;

import java.util.List;

import org.codehaus.jackson.node.ObjectNode;
import org.xbmc.android.jsonrpc.api.AbstractCall;
import org.xbmc.android.jsonrpc.io.ApiException;
import org.xbmc.android.jsonrpc.io.JsonApiRequest;

/**
 * Xbmc json-rpc utils.
 *
 * @author Erwan Le Huitouze <erwan.lehuitouze@gmail.com>
 *
 */
public class XbmcJson {
	/**
	 * Url of xbmc server.
	 */
	private String url;
	/**
	 * User of xbmc server.
	 */
	private String user;
	/**
	 * Password of xbmc server.
	 */
	private String password;

	/**
	 *
	 * @param url Url of xbmc server.
	 * @param user User of xbmc server.
	 * @param password Password of xbmc server.
	 */
	public XbmcJson(String url, String user, String password) {
		this.url = url;
		this.user = user;
		this.password = password;
	}

	/**
	 * Call xbmc json-rpc and return single result.
	 * @param caller
	 * @return T
	 */
	public <T> T getResult(AbstractCall<T> caller) {
		T result = null;

		this.executeCall(caller);
		result = caller.getResult();

		return result;
	}

	/**
	 * Call xbmc json-rpc and return list.
	 * @param caller
	 * @return T
	 */
	public <T> List<T> getResults(AbstractCall<T> caller) {
		List<T> result = null;

		this.executeCall(caller);
		result = caller.getResults();

		return result;
	}

	/**
	 * Execute call to xbmc server.
	 * @param caller
	 */
	private void executeCall(AbstractCall<?> caller) {
		try {
			ObjectNode object = JsonApiRequest.execute(
					this.url,
					this.user,
					this.password,
					caller.getRequest());

			caller.setResponse(object);
		} catch (ApiException e) {

		}
	}
}
