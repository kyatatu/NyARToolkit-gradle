/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2012 Ryo Iizuka
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.rasterdriver.pixel;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.raster.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.*;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * このインタフェイスは、ラスタからRGBピクセル値を読みだす関数を定義します。 {@link INyARRgbRaster}
 * インタフェイスを実装したクラスで使うことを想定しています。 RGBデータは、0から255までの範囲を持ちます。
 * <p>
 * このインタフェイスは、画素アクセサとして高頻度でコールされることが予想されます。そのため、画素形式のマッチング等は排除するようにしてください。
 * </p>
 */
public interface INyARRgbPixelDriver
{
	/**
	 * この関数は、ピクセルドライバの参照するラスタのサイズを返します。
	 * @return
	 * [readonly]
	 */
	public NyARIntSize getSize();
	/**
	 * この関数は、指定した座標の1ピクセル分のRGBデータを、配列に格納して返します。
	 * 実装クラスでは、バッファから指定した座標のRGB値を取得する処理を実装してください。
	 * 
	 * @param i_x
	 *            取得するピクセルの座標。画像の範囲内である事。
	 * @param i_y
	 *            取得するピクセルの座標。画像の範囲内である事。
	 * @param i_rgb
	 *            ピクセル値を返却する配列を指定します。3要素以上の配列が必要です。 値は、[R][G][B]の順に格納します。
	 * @throws NyARRuntimeException
	 */
	public void getPixel(int i_x, int i_y, int[] i_rgb);

	/**
	 * この関数は、座標群から、ピクセルごとのRGBデータを、配列に格納して返します。
	 * 実装クラスでは、バッファから、座標軍のRGB値を取得する処理を実装してください。
	 * 
	 * @param i_x
	 *            取得するピクセルの座標配列。画像の範囲内である事。
	 * @param i_y
	 *            取得するピクセルの座標配列。画像の範囲内である事。
	 * @param i_num
	 *            取得するピクセルの数を指定します。
	 * @param i_intrgb
	 *            ピクセル値を返却する配列を指定します。3要素以上の配列が必要です。
	 *            値は、[R1][G1][B1][R2][G2][B2]の順に格納します。
	 * @throws NyARRuntimeException
	 */
	public void getPixelSet(int[] i_x, int[] i_y, int i_num, int[] i_intrgb)
			throws NyARRuntimeException;

	/**
	 * この関数は、RGBデータを指定した座標のピクセルにセットします。 実装クラスでは、バッファにRGB値を書込む処理を実装してください。
	 * 
	 * @param i_x
	 *            書込むピクセルの座標。画像の範囲内である事。
	 * @param i_y
	 *            書込むピクセルの座標。画像の範囲内である事。
	 * @param i_r
	 *            R成分のピクセル値。
	 * @param i_g
	 *            G成分のピクセル値。
	 * @param i_b
	 *            B成分のピクセル値。
	 * @throws NyARRuntimeException
	 */
	public void setPixel(int i_x, int i_y, int i_r, int i_g, int i_b)
			throws NyARRuntimeException;

	/**
	 * この関数は、RGBデータを指定した座標のピクセルにセットします。 実装クラスでは、バッファにRGB値を書込む処理を実装してください。
	 * 
	 * @param i_x
	 *            書込むピクセルの座標。画像の範囲内である事。
	 * @param i_y
	 *            書込むピクセルの座標。画像の範囲内である事。
	 * @param i_rgb
	 *            設定するピクセル値。3要素以上の配列が必要です。 値は、[R][G][B]の順に格納します。
	 * @throws NyARRuntimeException
	 */
	public void setPixel(int i_x, int i_y, int[] i_rgb);

	/**
	 * この関数は、座標群にピクセルごとのRGBデータをセットします。 実装クラスでは、バッファにRGB値を書込む処理を実装してください。
	 * 
	 * @param i_x
	 *            取得するピクセルの座標配列。画像の範囲内である事。
	 * @param i_y
	 *            取得するピクセルの座標配列。画像の範囲内である事。
	 * @param i_num
	 *            設定するピクセルの数を指定します。
	 * @param i_intrgb
	 *            設定するピクセル値を格納する配列を指定します。3×i_num要素以上の配列が必要です。
	 *            値は、[R1][G1][B1][R2][G2][B2]の順に格納します。
	 * @throws NyARRuntimeException
	 */
	public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intrgb);

	/**
	 * この関数は、新しいラスタをインスタンスにセットします。
	 * 
	 * @throws NyARRuntimeException
	 */
	public void switchRaster(INyARRgbRaster i_raster);
}

