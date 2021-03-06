package jp.nyatla.nyartoolkit.dev.pro.core.rasterfiltr;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.pixeldriver.INyARGsPixelDriver;
import jp.nyatla.nyartoolkit.core.raster.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

public class NyARDefocusFilterFactory
{
	/**
	 * ãã?®é¢æ°ã¯ãDefocusãã£ã«ã¿ã¼ãçæãã¾ãã??
	 * æ?é©åããã¦ã?ãå½¢å¼ã?¯ä»¥ä¸ã?®éãã§ãã??
	 * <ul>
	 * <li></li>
	 * </ul>
	 * @param i_raster
	 * @return
	 * @throws NyARException
	 */
	public static INyARDefocusFilter createDriver(INyARGrayscaleRaster i_raster) throws NyARException
	{
		switch(i_raster.getBufferType()){
		default:
			return new DefocusFilter_Any(i_raster);
		}
	}
}

class DefocusFilter_Any implements INyARDefocusFilter
{
	private INyARGrayscaleRaster _ref_raster;
	private INyARGrayscaleRaster _tmp_raster;
	public DefocusFilter_Any(INyARGrayscaleRaster i_raster) throws NyARException
	{
		this._ref_raster=i_raster;
	}
	public void doFilter(INyARGrayscaleRaster i_output,int i_loop) throws NyARException
	{
		assert(i_loop>0);
		assert(this._ref_raster!=i_output);
		switch(i_loop){
		case 1:
			this.doFilter(i_output);
			break;
		default:
			//BufferTypeãç°ãªããããã³ãã©ãªãããã¡ãç¡ã?å ´åã?¯ã?ã³ãã©ãªãããã¡ãæ§ç¯?
			if(this._tmp_raster==null || i_output.isEqualBufferType(this._tmp_raster.getBufferType())){
				NyARIntSize s=this._ref_raster.getSize();
				this._tmp_raster=new NyARGrayscaleRaster(s.w,s.h,NyARBufferType.INT1D_GRAY_8,true);
			}
			//loop
			if(i_loop%2==0){
				this.doFilter(this._tmp_raster);
			}else{
				this.doFilter(i_output);
			}
			//ã¤ã³ã¿ãã§ã¤ã¹ãã ããã?
			INyARDefocusFilter dft=new DefocusFilter_Any(this._tmp_raster);
			INyARDefocusFilter dfo=new DefocusFilter_Any(i_output);
			//dstã«åºåæ?¼ç´?
			for(int i=i_loop-2;i>=0;i--){
				if(i%2==1){
					//out raster
					dfo.doFilter(this._tmp_raster);
				}else{
					//tmp laster
					dft.doFilter(i_output);
				}
			}
			break;
		}
		return;
	}
	
	
	public void doFilter(INyARGrayscaleRaster i_output) throws NyARException
	{
		assert(i_output!=this._ref_raster);
	    NyARIntSize s=this._ref_raster.getSize();
        //edgeé¨å?ã¯ä½ãããªã?ãã??
        INyARGsPixelDriver src=this._ref_raster.getGsPixelDriver();
        INyARGsPixelDriver dst=i_output.getGsPixelDriver();
        for(int i=s.w-1;i>=0;i--){
        	dst.setPixel(i, 0,src.getPixel(i,0));
        	dst.setPixel(i,s.h-1,src.getPixel(i,s.h-1));
        }
        for(int i=s.h-1;i>=1;i--){
        	dst.setPixel(0, i,src.getPixel(0,i));
        	dst.setPixel(s.w-1,i,src.getPixel(s.w-1,i));
        }
        //ã¨ã?ã¸ãé¤ã?ãé¨å?
        for(int y=s.h-2;y>0;y--){
	        for(int x=s.w-2;x>0;x--){
                dst.setPixel(x,y,
                		(src.getPixel(x-1,y-1)+src.getPixel(x  ,y-1)+src.getPixel(x+1,y-1)+
                		src.getPixel(x-1,y  )+src.getPixel(x  ,y  )+src.getPixel(x+1,y  )+
                		src.getPixel(x-1,y+1)+src.getPixel(x  ,y+1)+src.getPixel(x+1,y+1))/9);
	        }
        } 
	}
}