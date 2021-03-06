/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2009 Ryo Iizuka
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.dev.pro.markersytem;

import java.io.InputStream;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * ãã?®ã¯ã©ã¹ã¯ãNyARToolkitã®å§¿å¢æ¨å®ã¢ã«ã´ãªãºã?ã«èª¿æ´ããã³ã³ãã£ã®ã¥ã¬ã¼ã·ã§ã³ã¯ã©ã¹ã§ãã??
 *
 */
public class NyARNFTSystemConfig
{
	protected NyARParam _param;
	/**
	 * ã³ã³ã¹ãã©ã¯ã¿ã§ãã??
	 * åæåæ¸ã«ã¡ã©ãã©ã¡ã¼ã¿ããã³ã³ãã£ã®ã¥ã¬ã¼ã·ã§ã³ãçæãã¾ãã??
	 * @param i_param
	 * åæåã«ä½¿ã?ã«ã¡ã©ãã©ã¡ã¼ã¿ãªãã¸ã§ã¯ãã?ã¤ã³ã¹ã¿ã³ã¹ã®æ?ææ¨©ã¯ãã¤ã³ã¹ã¿ã³ã¹ã«ç§»ãã¾ãã??
	 */
	public NyARNFTSystemConfig(NyARParam i_param)
	{
		this._param=i_param;
	}
	/**
	 * ã³ã³ã¹ãã©ã¯ã¿ã§ãã??
	 * i_ar_parama_streamããã«ã¡ã©ãã©ã¡ã¼ã¿ãã¡ã¤ã«ãèª­ã¿åºãã¦ãã¹ã¯ãªã¼ã³ãµã¤ãºãi_width,i_heightã«å¤å½¢ãã¦ããã?
	 * ã³ã³ãã£ã®ã¥ã¬ã¼ã·ã§ã³ãçæãã¾ãã??
	 * @param i_ar_param_stream
	 * ã«ã¡ã©ãã©ã¡ã¼ã¿ãã¡ã¤ã«ãèª­ã¿åºãã¹ããªã¼ã?
	 * @param i_width
	 * ã¹ã¯ãªã¼ã³ãµã¤ãº
	 * @param i_height
	 * ã¹ã¯ãªã¼ã³ãµã¤ãº
	 * @throws NyARException
	 */
	public NyARNFTSystemConfig(InputStream i_ar_param_stream,int i_width,int i_height) throws NyARException
	{
		this._param=NyARParam.createFromARParamFile(i_ar_param_stream);
		this._param.changeScreenSize(i_width,i_height);
	}
	/**
	 * ã³ã³ã¹ãã©ã¯ã¿ã§ãã?ã«ã¡ã©ãã©ã¡ã¼ã¿ã«ãµã³ãã«å¤(../Data/camera_para.dat)ã®å¤ãã­ã¼ããã¦ã?
	 * ã³ã³ãã£ã®ã¥ã¬ã¼ã·ã§ã³ãçæãã¾ãã??
	 * @param i_width
	 * ã¹ã¯ãªã¼ã³ãµã¤ãº
	 * @param i_height
	 * ã¹ã¯ãªã¼ã³ãµã¤ãº
	 * @throws NyARException
	 */
	public NyARNFTSystemConfig(int i_width,int i_height) throws NyARException
	{
		this._param=NyARParam.createDefaultParameter();
		this._param.changeScreenSize(i_width,i_height);		
	}
	/**
	 * ãã?®å¤ã¯ãã«ã¡ã©ãã©ã¡ã¼ã¿ã®ã¹ã¯ãªã¼ã³ãµã¤ãºã§ãã??
	 */
	public final NyARIntSize getScreenSize()
	{
		return this._param.getScreenSize();
	}
	/**
	 * @Override
	 */
	public NyARParam getNyARParam()
	{
		return 	this._param;
	}
}