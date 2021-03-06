package jp.nyatla.nyartoolkit.dev.pro.markersytem;

import java.io.InputStream;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResultParam;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix44;
import jp.nyatla.nyartoolkit.markersystem.NyARSensor;
import jp.nyatla.nyartoolkit.markersystem.NyARSingleCameraSystem;
import jp.nyatla.nyartoolkit.pro.core.kpm.NyARKpmDataSet;
import jp.nyatla.nyartoolkit.pro.core.kpm.NyARSingleKpm;
import jp.nyatla.nyartoolkit.pro.core.kpm.ann.NyARSurfAnnMatch;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.NyARSurfaceDataSet;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.NyARSurfaceTracker;
import jp.nyatla.nyartoolkit.pro.core.transmat.NyARNftTransMatUtils;

/**
 * 縺薙?ｮ繧ｯ繝ｩ繧ｹ縺ｯ縲?1縺､縺ｮ迚ｹ蠕ｴ轤ｹ繧ｻ繝?繝医?ｮ荳画ｬ｡蜈?蠎ｧ讓吶ｒ謗ｨ螳壹＠縺ｾ縺吶??
 *
 */
public class NyARSingleNFTSystem extends NyARSingleCameraSystem
{
	public final static int MAX_RANSAC_RESULT = 200;
	public final static int MAX_SURFACE_TRACKING=20;
	private NyARSingleKpm _kpm;
	private NyARSurfaceTracker _stracker;
	private NyARNftTransMatUtils _transmat_utils;
	private int _tick=0;
	private static int[] _area_table={jp.nyatla.nyartoolkit.dev.pro.core.kpm.AREA_QLT,jp.nyatla.nyartoolkit.dev.pro.core.kpm.AREA_QLB,jp.nyatla.nyartoolkit.dev.pro.core.kpm.AREA_QRT,jp.nyatla.nyartoolkit.dev.pro.core.kpm.AREA_QRB,jp.nyatla.nyartoolkit.dev.pro.core.kpm.AREA_QCE};
	private NyARDoubleMatrix44 _current_transmat=new NyARDoubleMatrix44();
	private boolean _is_found=false;
	private NyARKpmDataSet _rds;
	private NyARSurfaceDataSet _ss;
	private NyARNFTSystemConfig _config;
	
	public NyARSingleNFTSystem(NyARNFTSystemConfig i_config) throws NyARException
	{
		super(i_config.getNyARParam());
		this._config=i_config;
		this._stracker=new NyARSurfaceTracker(i_config.getNyARParam(),MAX_SURFACE_TRACKING);
		this._transmat_utils=new NyARNftTransMatUtils(i_config.getNyARParam(),MAX_RANSAC_RESULT);
	}
	/**
	 * 縺薙?ｮ髢｢謨ｰ縺ｯ縲＋@link InputStream}縺九ｉ迚ｹ蠕ｴ繧ｻ繝?繝医ｒ隱ｭ縺ｿ蜃ｺ縺励※縲√う繝ｳ繧ｹ繧ｿ繝ｳ繧ｹ縺ｫ繧ｻ繝?繝医＠縺ｾ縺吶??
	 * @param i_iset
	 * 繧ｵ繝ｼ繝輔ぉ繧､繧ｹ逕ｻ蜒上ヵ繧｡繧､繝ｫ繧定ｪｭ縺ｿ蜃ｺ縺几@link InputStream}
	 * @param i_fset
	 * 繧ｵ繝ｼ繝輔ぉ繧､繧ｹ迚ｹ蠕ｴ繝輔ぃ繧､繝ｫ繧定ｪｭ縺ｿ蜃ｺ縺几@link InputStream}
	 * @param i_kpm_fset
	 * 繧ｭ繝ｼ繝昴う繝ｳ繝医ヵ繧｡繧､繝ｫ繧定ｪｭ縺ｿ蜃ｺ縺几@link InputStream}
	 * @throws NyARException
	 */
	public void setARNftDataset(InputStream i_iset,InputStream i_fset,InputStream i_kpm_fset) throws NyARException
	{
		this._ss=NyARSurfaceDataSet.loadFromSurfaceFiles(i_iset,i_fset);
		this._rds=NyARKpmDataSet.loadFromFset2(i_kpm_fset);
		this._kpm=new NyARSingleKpm(this._config.getNyARParam(),this._rds);
	}
	public void update(NyARSensor i_sensor) throws NyARException
	{
		if(this._is_found){
			if(updateTracking(i_sensor)){
				return;
			}
			if(this.updateKpm(i_sensor)){
				this._stracker.resetLog();
				return;
			}
			this._is_found=false;
			return;
		}else{
			if(this.updateKpm(i_sensor)){
				this._stracker.resetLog();
				this._is_found=true;
				return;
			}
			return;
		}
	}
	public boolean isExist()
	{
		return this._is_found;
	}
	/**
	 * [readonly]
	 * 迴ｾ蝨ｨ縺ｮ蟋ｿ蜍｢螟画鋤陦悟?励ｒ霑斐＠縺ｾ縺吶??{@link #isExist()}縺荊rue縺ｮ譎ゅ?ｮ縺ｿ菴ｿ逕ｨ縺ｧ縺阪∪縺吶??
	 * @param i_mat
	 * @return
	 */
	public NyARDoubleMatrix44 getMarkerMatrix() throws NyARException
	{
		if(!this._is_found){
			throw new NyARException();
		}
		return this._current_transmat;
	}
	private NyARDoublePoint2d[] __pos2d = NyARDoublePoint2d.createArray(MAX_SURFACE_TRACKING);
	private NyARDoublePoint3d[] __pos3d = NyARDoublePoint3d.createArray(MAX_SURFACE_TRACKING);
	private NyARTransMatResultParam _tresult=new NyARTransMatResultParam();
	/**
	 * SurfaceTracking縺ｫ繧医ｋ讀懷?ｺ
	 * @param i_sensor
	 * @return
	 * @throws NyARException
	 */
	public boolean updateTracking(NyARSensor i_sensor) throws NyARException
	{
		int points=this._stracker.tracking(i_sensor.getGsImage(),this._ss,this._current_transmat,this.__pos2d,this.__pos3d,MAX_SURFACE_TRACKING);
		if(points<4){
			return false;
		}
		return this._transmat_utils.surfaceTrackingTransmat(this._current_transmat,this.__pos2d,this.__pos3d, points, this._current_transmat,this._tresult);
		
	}
	/**
	 * KPM縺ｫ繧医ｋ蛻晄悄讀懷?ｺ
	 * @param i_sensor
	 * @return
	 * @throws NyARException
	 */
	public boolean updateKpm(NyARSensor i_sensor) throws NyARException
	{
		NyARSurfAnnMatch.ResultPtr match_items=new NyARSurfAnnMatch.ResultPtr(MAX_RANSAC_RESULT);
		this._kpm.updateMatching(i_sensor.getGsImage());
		if(this._kpm.getRansacMatchPoints(jp.nyatla.nyartoolkit.dev.pro.core.kpm.AREA_ALL, match_items)){
			if(_transmat_utils.kpmTransmat(match_items, this._current_transmat)){
				return true;
			}
		}
		this._tick=(this._tick+1)%0x0fffffff;
		//1/5縺ｮ遒ｺ邇?縺上ｉ縺?縺ｧ隱ｿ譟ｻ
		if(this._kpm.getRansacMatchPoints(_area_table[this._tick%5], match_items)){
			if(_transmat_utils.kpmTransmat(match_items, this._current_transmat)){
				return true;
			}
		}
		return false;
	}
	/**
	 * 縺薙?ｮ髢｢謨ｰ縺ｯ縲√せ繧ｯ繝ｪ繝ｼ繝ｳ蠎ｧ讓咏せ繧偵?槭?ｼ繧ｫ蟷ｳ髱｢縺ｮ轤ｹ縺ｫ螟画鋤縺励∪縺吶??
	 * {@link #isExist()}縺荊rue縺ｮ譎ゅ↓縺?縺台ｽｿ逕ｨ縺ｧ縺阪∪縺吶??
	 * @param i_x
	 * 螟画鋤蜈?縺ｮ繧ｹ繧ｯ繝ｪ繝ｼ繝ｳ蠎ｧ讓?
	 * @param i_y
	 * 螟画鋤蜈?縺ｮ繧ｹ繧ｯ繝ｪ繝ｼ繝ｳ蠎ｧ讓?
	 * @param i_out
	 * 邨先棡繧呈?ｼ邏阪☆繧九が繝悶ず繧ｧ繧ｯ繝?
	 * @return
	 * 邨先棡繧呈?ｼ邏阪＠縺殃_out縺ｫ險ｭ螳壹＠縺溘が繝悶ず繧ｧ繧ｯ繝?
	 */
	public NyARDoublePoint3d getMarkerPlanePos(int i_x,int i_y,NyARDoublePoint3d i_out) throws NyARException
	{
		this._frustum.unProjectOnMatrix(i_x, i_y,this.getMarkerMatrix(),i_out);
		return i_out;
	}
	private NyARDoublePoint3d _wk_3dpos=new NyARDoublePoint3d();
	/**
	 * 縺薙?ｮ髢｢謨ｰ縺ｯ縲√?槭?ｼ繧ｫ蠎ｧ讓咏ｳｻ縺ｮ轤ｹ繧偵せ繧ｯ繝ｪ繝ｼ繝ｳ蠎ｧ讓吶∈螟画鋤縺励∪縺吶??
	 * {@link #isExistMarker(int)}縺荊rue縺ｮ譎ゅ↓縺?縺台ｽｿ逕ｨ縺ｧ縺阪∪縺吶??
	 * @param i_x
	 * 繝槭?ｼ繧ｫ蠎ｧ讓咏ｳｻ縺ｮX蠎ｧ讓?
	 * @param i_y
	 * 繝槭?ｼ繧ｫ蠎ｧ讓咏ｳｻ縺ｮY蠎ｧ讓?
	 * @param i_z
	 * 繝槭?ｼ繧ｫ蠎ｧ讓咏ｳｻ縺ｮZ蠎ｧ讓?
	 * @param i_out
	 * 邨先棡繧呈?ｼ邏阪☆繧九が繝悶ず繧ｧ繧ｯ繝?
	 * @return
	 * 邨先棡繧呈?ｼ邏阪＠縺殃_out縺ｫ險ｭ螳壹＠縺溘が繝悶ず繧ｧ繧ｯ繝?
	 */
	public NyARDoublePoint2d getScreenPos(double i_x,double i_y,double i_z,NyARDoublePoint2d i_out) throws NyARException
	{
		NyARDoublePoint3d _wk_3dpos=this._wk_3dpos;
		this.getMarkerMatrix().transform3d(i_x, i_y, i_z,_wk_3dpos);
		this._frustum.project(_wk_3dpos,i_out);
		return i_out;
	}	

	
	/**
	 * 縺薙?ｮ髢｢謨ｰ縺ｯ縲√?槭?ｼ繧ｫ蟷ｳ髱｢荳翫?ｮ莉ｻ諢上?ｮ?ｼ皮せ縺ｧ蝗ｲ縺ｾ繧後ｋ鬆伜沺縺九ｉ縲∫判蜒上ｒ蟆?蠖ｱ螟画鋤縺励※霑斐＠縺ｾ縺吶??
	 * {@link #isExist()}縺荊rue縺ｮ譎ゅ↓縺?縺台ｽｿ逕ｨ縺ｧ縺阪∪縺吶??
	 * @param i_sensor
	 * 逕ｻ蜒上ｒ蜿門ｾ励☆繧九そ繝ｳ繧ｵ繧ｪ繝悶ず繧ｧ繧ｯ繝医?る?壼ｸｸ縺ｯ{@link #update(NyARSensor)}髢｢謨ｰ縺ｫ蜈･蜉帙＠縺溘ｂ縺ｮ縺ｨ蜷後§繧ゅ?ｮ繧呈欠螳壹＠縺ｾ縺吶??
	 * @param i_x1
	 * 鬆らせ1[mm]
	 * @param i_y1
	 * 鬆らせ1[mm]
	 * @param i_x2
	 * 鬆らせ2[mm]
	 * @param i_y2
	 * 鬆らせ2[mm]
	 * @param i_x3
	 * 鬆らせ3[mm]
	 * @param i_y3
	 * 鬆らせ3[mm]
	 * @param i_x4
	 * 鬆らせ4[mm]
	 * @param i_y4
	 * 鬆らせ4[mm]
	 * @param i_raster
	 * 蜿門ｾ励＠縺溽判蜒上ｒ譬ｼ邏阪☆繧九が繝悶ず繧ｧ繧ｯ繝?
	 * @return
	 * 邨先棡繧呈?ｼ邏阪＠縺殃_raster繧ｪ繝悶ず繧ｧ繧ｯ繝?
	 * @throws NyARException
	 */
	public INyARRgbRaster getMarkerPlaneImage(
		NyARSensor i_sensor,
		double i_x1,double i_y1,
		double i_x2,double i_y2,
		double i_x3,double i_y3,
		double i_x4,double i_y4,
	    INyARRgbRaster i_raster) throws NyARException
	{
		NyARDoublePoint3d[] pos  = this.__pos3d;
		NyARDoublePoint2d[] pos2 = this.__pos2d;
		NyARDoubleMatrix44 tmat=this.getMarkerMatrix();
		tmat.transform3d(i_x1, i_y1,0,	pos[1]);
		tmat.transform3d(i_x2, i_y2,0,	pos[0]);
		tmat.transform3d(i_x3, i_y3,0,	pos[3]);
		tmat.transform3d(i_x4, i_y4,0,	pos[2]);
		for(int i=3;i>=0;i--){
			this._frustum.project(pos[i],pos2[i]);
		}
		return i_sensor.getPerspectiveImage(pos2[0].x, pos2[0].y,pos2[1].x, pos2[1].y,pos2[2].x, pos2[2].y,pos2[3].x, pos2[3].y,i_raster);
	}
	/**
	 * 縺薙?ｮ髢｢謨ｰ縺ｯ縲√?槭?ｼ繧ｫ蟷ｳ髱｢荳翫?ｮ莉ｻ諢上?ｮ遏ｩ蠖｢縺ｧ蝗ｲ縺ｾ繧後ｋ鬆伜沺縺九ｉ縲∫判蜒上ｒ蟆?蠖ｱ螟画鋤縺励※霑斐＠縺ｾ縺吶??
	 * {@link #isExistMarker(int)}縺荊rue縺ｮ譎ゅ↓縺?縺台ｽｿ逕ｨ縺ｧ縺阪∪縺吶??
	 * @param i_sensor
	 * 逕ｻ蜒上ｒ蜿門ｾ励☆繧九そ繝ｳ繧ｵ繧ｪ繝悶ず繧ｧ繧ｯ繝医?る?壼ｸｸ縺ｯ{@link #update(NyARSensor)}髢｢謨ｰ縺ｫ蜈･蜉帙＠縺溘ｂ縺ｮ縺ｨ蜷後§繧ゅ?ｮ繧呈欠螳壹＠縺ｾ縺吶??
	 * @param i_l
	 * 遏ｩ蠖｢縺ｮ蟾ｦ荳顔せ縺ｧ縺吶??
	 * @param i_t
	 * 遏ｩ蠖｢縺ｮ蟾ｦ荳顔せ縺ｧ縺吶??
	 * @param i_w
	 * 遏ｩ蠖｢縺ｮ蟷?縺ｧ縺吶??
	 * @param i_h
	 * 遏ｩ蠖｢縺ｮ蟷?縺ｧ縺吶??
	 * @param i_raster
	 * 蜃ｺ蜉帛?医?ｮ繧ｪ繝悶ず繧ｧ繧ｯ繝?
	 * @return
	 * 邨先棡繧呈?ｼ邏阪＠縺殃_raster繧ｪ繝悶ず繧ｧ繧ｯ繝?
	 * @throws NyARException
	 */
	public INyARRgbRaster getMarkerPlaneImage(
		NyARSensor i_sensor,
	    double i_l,double i_t,
	    double i_w,double i_h,
	    INyARRgbRaster i_raster) throws NyARException
    {
		return this.getMarkerPlaneImage(i_sensor,i_l+i_w-1,i_t+i_h-1,i_l,i_t+i_h-1,i_l,i_t,i_l+i_w-1,i_t,i_raster);
    }	
}
