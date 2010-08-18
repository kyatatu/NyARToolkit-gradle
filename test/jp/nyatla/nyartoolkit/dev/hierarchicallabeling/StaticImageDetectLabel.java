package jp.nyatla.nyartoolkit.dev.hierarchicallabeling;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.Date;

import javax.imageio.ImageIO;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARLabeling_Rle;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARRleLabelFragmentInfo;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.NyARRasterFilter_Rgb2Gs_RgbAve;
import jp.nyatla.nyartoolkit.core.rasterreader.NyARVectorReader_INT1D_GRAY_8;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARContourPickup;
import jp.nyatla.nyartoolkit.utils.j2se.NyARRasterImageIO;
import jp.nyatla.nyartoolkit.core.types.*;


import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster_RGB;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.*;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ContourTargetSrc.ContourTargetSrcItem;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour.ContourTargetList;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.contour.ContourTargetList.ContourTargetItem;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget.IgnoreTargetList;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget.IgnoreTargetSrc;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget.IgnoreTracking;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.ignoretarget.IgnoreTargetList.IgnoreTarget;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTargetList;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTargetSrc;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTracking;
import jp.nyatla.nyartoolkit.dev.hierarchicallabeling.tracking.newtarget.NewTargetList.NewTargetItem;



class MyDetector extends HierarchyLabeling
{
	public Graphics g;
	private final NyARContourPickup _cpickup=new NyARContourPickup();
	private NyARIntPoint2d[] _coord;


	
	
	
	public MyDetector(int i_width,int i_height,int i_depth,int i_raster_type) throws NyARException
	{
		super(i_width,i_height,i_depth,i_raster_type);
		this._coord=NyARIntPoint2d.createArray((i_width+i_height)*2);
		this._newtargetsrc=new NewTargetSrc(10);
		this._newtarget=new NewTargetList(10);
		this._ignoretargetsrc=new IgnoreTargetSrc(10);
		this._ignoretarget=new IgnoreTargetList(10);
		this._contouretarget= new ContourTargetList(10);
		for(int i=0;i<2;i++){
			this._area_holders[i]=new AreaTargetSrcHolder(100);
			this._contoure_holders[i]=new ContourTargetSrcHolder(10,i_width+i_height*2);
		}
		this._current_holder_page=0;
	}
	public void detectOutline(NyARGrayscaleRaster i_raster,int i_th) throws NyARException
	{
		//Holderのページを設定
		this._current_holder_page=(this._current_holder_page+1)%2;
		this._current_areaholder=this._area_holders[this._current_holder_page];
		this._current_contoureholder=this._contoure_holders[this._current_holder_page];
		

		this._base_gs=i_raster;
		//Srcを編集
		super.detectOutline(i_raster,i_th);
		//アップグレード処理
		//New->Ignore(100フレーム無視が続いた場合)
		
	}
	NyARGrayscaleRaster _base_gs;

	/**
	 * 2ページ分のソースデータホルダ
	 */
	private AreaTargetSrcHolder[] _area_holders=new AreaTargetSrcHolder[2];
	private ContourTargetSrcHolder[] _contoure_holders=new ContourTargetSrcHolder[2];
	private AreaTargetSrcHolder _current_areaholder;
	private ContourTargetSrcHolder _current_contoureholder;
	private int _current_holder_page;

	
	
	NewTargetSrc _newtargetsrc;
	NewTargetList _newtarget;
	
	IgnoreTargetSrc _ignoretargetsrc;
	IgnoreTargetList _ignoretarget;
	
	ContourTargetList _contouretarget;
	
	
	
	
	protected void onLabelFound(HierarchyRect i_imgmap,NyARGrayscaleRaster i_raster,int i_th,NyARRleLabelFragmentInfo info) throws NyARException
	{
		//一旦新規リストのソースに登録
		AppearTargetSrc.AppearSrcItem item=this._appeartargetsrc.pushSrcTarget(i_imgmap, info);
		if(item==null){
			return;
		}
		int match_index;
		//dispatch infomation
		
		//Level3 Tracker(認識済・矩形レベルで追尾中)で処理？
		//Level2 Tracker(認識待・矩形レベルで追尾中)で処理？
		//Level2　Tracker(輪郭特定予約・範囲レベルで追尾中)で処理で追尾中
		match_index=this._contouretarget.getMatchTargetIndex(item);
		if(match_index>=0){
			ContourTargetSrcItem contoure_item=this._contouretargetsrc.pushTarget(item, i_imgmap, i_raster, i_th, info);
			//対象の輪郭？
			if(this._contouretarget.getMatchTargetIndex(contoure_item)!=-1){
				//輪郭ソースに補足したので、出現ソースを解除
				this._appeartargetsrc.pop();
				return;
			}else{
				this._contouretargetsrc.pop();
			}
		}
		

		//Level1 Tracker(Newレベル)で処理中ならここまで
		match_index=this._newtarget.getMatchTargetIndex(item);

		//範囲リストに既に存在するなら、範囲srcに入力して、新規srcから外す。
		if(match_index>=0){
			this._newtargetsrc.pushSrcTarget(item);
			this._appeartargetsrc.pop();
			return;
		}
		
		//Level0 Tracker(無視リスト)で処理中なら除外
		match_index=this._ignoretarget.getMatchTargetIndex(item);

		//無視リストに既に存在するなら、無視srcに入力して、新規srcから外す。
		if(match_index>=0){
			this._ignoretargetsrc.pushSrcTarget(item);
			this._appeartargetsrc.pop();
			return;
		}
		
		//昇格処理
		/*
		

		//輪郭線を出す
		int n=this._cpickup.getContour(i_raster,i_th,info.entry_x,info.clip_t,this._coord);
		//元画像からベクトルを拾う。
		NyARVectorReader_INT1D_GRAY_8 reader=new NyARVectorReader_INT1D_GRAY_8(this._base_gs);
		NyARIntRect tmprect=new NyARIntRect();
		tmprect.w=skip*2;
		tmprect.h=skip*2;

		//ベクトル配列を作る
		NyARVectorReader_INT1D_GRAY_8.NyARDoublePosVec2d pv=new NyARVectorReader_INT1D_GRAY_8.NyARDoublePosVec2d();
		NyARVectorReader_INT1D_GRAY_8.NyARDoublePosVec2d[] pva= NyARVectorReader_INT1D_GRAY_8.createArray(n);*/
/*
		int number_of_data=1;
		//0個目のベクトル
		tmprect.x=i_imgmap.x+(this._coord[0].x-1)*skip;
		tmprect.y=i_imgmap.y+(this._coord[0].y-1)*skip;
		reader.getAreaVector8(tmprect,pva[0]);
		//ベクトルデータを作成
		for(int i=1;i<n;i++){
//ベクトル定義矩形を作る。
			tmprect.x=i_imgmap.x+(this._coord[i].x-1)*skip;
			tmprect.y=i_imgmap.y+(this._coord[i].y-1)*skip;
//矩形の位置をずらさないとね
//クリップ位置の補正
			//ベクトル取得
			reader.getAreaVector8(tmprect,pva[number_of_data]);
			g.fillRect((int)pva[number_of_data].x,(int)pva[number_of_data].y,1,1);
			
			//類似度判定
			if(getVecCos(pva[number_of_data-1],pva[number_of_data])<0.99){
				//相関なし
				number_of_data++;
			}else{
				//相関あり
				pva[number_of_data-1].x=(pva[number_of_data-1].x+pva[number_of_data].x)/2;
				pva[number_of_data-1].y=(pva[number_of_data-1].y+pva[number_of_data].y)/2;
				pva[number_of_data-1].dx=(pva[number_of_data-1].dx+pva[number_of_data].dx);
				pva[number_of_data-1].dy=(pva[number_of_data-1].dy+pva[number_of_data].dy);
			}
		}
		//ベクトルの描画
		for(int i=0;i<number_of_data;i++){
			double sin=pva[i].dy/Math.sqrt(pva[i].dx*pva[i].dx+pva[i].dy*pva[i].dy);
			double cos=pva[i].dx/Math.sqrt(pva[i].dx*pva[i].dx+pva[i].dy*pva[i].dy);
			double l=Math.sqrt(pva[i].dx*pva[i].dx+pva[i].dy*pva[i].dy)/16;
			g.setColor(Color.BLUE);
			g.drawLine((int)pva[i].x,(int)pva[i].y,(int)(pva[i].x+l*cos),(int)(pva[i].y+l*sin));				
		}*/
		
		
		//検出矩形を定義する。
		//l*skip-skip,t*skip-skip,r+skip,b+skip
		g.setColor(Color.green);

//		int skip=i_imgmap.dot_skip;
		//System.out.println(i_imgmap.dot_skip+":"+i_imgmap.id+","+(info.clip_l*skip+i_imgmap.x)+","+(info.clip_t*skip+i_imgmap.y));			
//		BufferedImage sink = new BufferedImage(i_raster.getWidth(), i_raster.getHeight(), ColorSpace.TYPE_RGB);
//		NyARRasterImageIO.copy(i_raster, sink);
/*			g.drawImage(
				sink,
				i_imgmap.x,i_imgmap.y,
				sink.getWidth()*skip,sink.getHeight()*skip,
				null);
*///			g.drawRect(
//				info.clip_l*skip+i_imgmap.x,
//				info.clip_t*skip+i_imgmap.y,
//				(info.clip_r-info.clip_l)*skip,
//				(info.clip_b-info.clip_t)*skip);
		
	}

	
}







/**
 * @todo
 * 矩形の追跡は動いてるから、位置予測機能と組み合わせて試すこと。
 *
 */

public class StaticImageDetectLabel extends Frame implements MouseMotionListener
{
	class TestL extends NyARLabeling_Rle
	{
		Graphics g;

		public TestL(int i_width,int i_height) throws NyARException
		{
			super(i_width,i_height);
		}
		protected void onLabelFound(NyARRleLabelFragmentInfo i_label)
		{
			g.setColor(Color.red);
			g.drawRect(
					i_label.clip_l,
					i_label.clip_t,
					(i_label.clip_r-i_label.clip_l),
					(i_label.clip_b-i_label.clip_t));			
		}
	}

	private final static String SAMPLE_FILES = "../Data/test.jpg";

	private static final long serialVersionUID = -2110888320986446576L;


	private int W = 320;
	private int H = 240;
	BufferedImage _src_image;

	public StaticImageDetectLabel() throws NyARException, Exception
	{
//		setTitle("Estimate Edge Sample");
		Insets ins = this.getInsets();
		this.setSize(1024 + ins.left + ins.right, 768 + ins.top + ins.bottom);

		_src_image = ImageIO.read(new File(SAMPLE_FILES));
		addMouseMotionListener(this);

		return;
	}
	int mouse_x;
	int mouse_y;
    public void mouseMoved(MouseEvent A00)
    {
        mouse_x = A00.getX();
        mouse_y = A00.getY();
        this.paint(this.getGraphics());
    }

    public void mouseDragged(MouseEvent A00) {}
	private Graphics g2;


	
	
	public void draw(INyARRgbRaster i_raster)
	{
	}
	

	

	private NewTracking _newtracking=new NewTracking(10);
	private IgnoreTracking _ignoretrack=new IgnoreTracking(10);
	static long tick;
	MyDetector _psd=new MyDetector(320,240,4,NyARBufferType.BYTE1D_R8G8B8_24);
    public void update(Graphics g,BufferedImage buf)
    {
    	tick++;
		try {
			
			Insets ins = this.getInsets();
			INyARRgbRaster ra =new NyARRgbRaster_RGB(320,240);
			NyARRasterImageIO.copy(buf,ra);
			//GS値化
			NyARGrayscaleRaster gs=new NyARGrayscaleRaster(320,240);
			NyARGrayscaleRaster ro=new NyARGrayscaleRaster(320,240);
			NyARRasterFilter_Rgb2Gs_RgbAve filter=new NyARRasterFilter_Rgb2Gs_RgbAve(ra.getBufferType());
			filter.doFilter(ra,gs);
			//

			MyDetector psd=this._psd;
			//GS画像の描画
			BufferedImage sink = new BufferedImage(ra.getWidth(), ra.getHeight(), ColorSpace.TYPE_RGB);
			NyARRasterImageIO.copy(gs, sink);
			
			psd._appeartargetsrc.clear();
			psd._newtargetsrc.clear();
			psd._ignoretargetsrc.clear();

			//一次検出
			psd.g=sink.getGraphics();
			psd.ins=this.getInsets();
			System.out.println("start---------");
			Date d2 = new Date();
//			for (int i = 0; i < 10000; i++) {
				psd.detectOutline(gs,30);
//			}
			Date d = new Date();
			System.out.println("H"+(d.getTime() - d2.getTime()));
			//トラック処理
			{
				this._newtracking.updateTrackTargetBySrc(tick,psd._newtargetsrc,psd._newtarget);
				this._ignoretrack.updateTrackTargetBySrc(tick,psd._ignoretargetsrc,psd._ignoretarget);
			}
			//アップデート処理
			//newtarget->ignore,coord
			for(int i=psd._newtargetsrc.getLength()-1;i>=0;i--)
			{
				//newtargetで、ageが100超えてもなにもされないならignoreに移動
				if(psd._newtarget.getItem(i).age>100){
					IgnoreTarget ig=psd._ignoretarget.pushTarget(psd._newtarget.getItem(i));
					if(ig==null){
						//失敗リストがいっぱいなら何もしない
						continue;
					}
					psd._newtarget.removeIgnoreOrder(i);
					continue;
				}
				//newtarget->coordの昇格処理 ageが50超えてたら
				if(psd._newtarget.getItem(i).age>50){
					ContourTargetItem ct=psd._contouretarget.pushTarget(psd._newtarget.getItem(i));
					if(ct==null){
						//失敗リストがいっぱいなら何もしない
						continue;
					}
					psd._newtarget.removeIgnoreOrder(i);
					continue;
				}				
			}
			//coord->ignore,rect
			for(int i=psd._contouretarget.getLength()-1;i>=0;i--)
			{
				//newtargetで、ageが100超えてもなにもされないならignoreに移動
				if(psd._contouretarget.getItem(i).age>100){
					IgnoreTarget ig=psd._ignoretarget.pushTarget(psd._contouretarget.getItem(i));
					if(ig==null){
						//失敗リストがいっぱいなら何もしない
						continue;
					}
					psd._contouretarget.removeIgnoreOrder(i);
					continue;
				}
				//coord->rect
			}
			

			//appeartarget->newtarget
			for(int i=psd._appeartargetsrc.getLength()-1;i>=0;i--)
			{
				//無条件にnewtargetへ追記
				NewTargetItem newtarget=psd._newtarget.pushTarget(tick,psd._appeartargetsrc.getItem(i));
				if(newtarget==null){
					//認識待ちがいっぱいなら何もしない。
					break;
				}
			}
			//coord->square
			//square->marker
			
			//結果表示
			
			
	/*		
			TestL te=new TestL(320,240);
			te.g=psd.g;
			d2 = new Date();
			for (int i = 0; i < 1000; i++) {
				te.labeling(gs,50);
			}
			d = new Date();
			System.out.println("L="+(d.getTime() - d2.getTime()));
			
	*/		
			
			//分析画像の座標計算
			int mx=mouse_x-ins.left;
			int my=mouse_y-ins.top;
			//画像を分析する。
			NyARIntRect tmprect=new NyARIntRect();
			tmprect.x=mx;
			tmprect.y=my;
			tmprect.w=8;
			tmprect.h=8;
			NyARDoublePoint2d pos=new NyARDoublePoint2d();
			NyARDoublePoint2d vec=new NyARDoublePoint2d();
			NyARVectorReader_INT1D_GRAY_8 reader=new NyARVectorReader_INT1D_GRAY_8(gs);			
/*			if(mx>0 && my>0){
				reader.getAreaVector8(tmprect,pos,vec);
			}
			//分析結果を描画
			double sin=vec.y/Math.sqrt(vec.x*vec.x+vec.y*vec.y);
			double cos=vec.x/Math.sqrt(vec.x*vec.x+vec.y*vec.y);
			Graphics g2=sink.getGraphics();
			g2.setColor(Color.BLUE);
			g2.drawLine((int)pos.x,(int)pos.y,(int)(pos.x+30*cos),(int)(pos.y+30*sin));
*/
			//
			{
				Graphics g2=sink.getGraphics();
				//無視リスト描画
				g2.setColor(Color.blue);
				for(int i=0;i<this._psd._ignoretarget.getLength();i++){
					NewTargetItem e=this._psd._newtarget.getItem(i);
					g2.drawRect(e.area.x, e.area.y, e.area.w, e.area.h);
				}
				//新規リスト描画
				g2.setColor(Color.red);
				for(int i=0;i<this._psd._newtarget.getLength();i++){
					
					NewTargetItem e=this._psd._newtarget.getItem(i);
					g2.drawRect(e.area.x, e.area.y, e.area.w, e.area.h);
				}
				sink.setRGB((int)pos.x,(int)pos.y,0xff0000);
				//輪郭リスト描画
				g2.setColor(Color.green);
				for(int i=0;i<this._psd._contouretarget.getLength();i++){
					
					ContourTargetItem e=this._psd._contouretarget.getItem(i);
					GraphicsTools.drawPolygon(g2,e., i_pt, i_number_of_pt)
					g2.drawPolygon(xPoints, yPoints, nPoints);
					g2.drawRect(e.area.x, e.area.y, e.area.w, e.area.h);
				}
				sink.setRGB((int)pos.x,(int)pos.y,0xff0000);
				
				
				g.drawImage(sink, ins.left, ins.top, this);
			}
//			g.drawImage(sink,ins.left,ins.top+240,ins.left+32,ins.top+240+32,mx,my,mx+8,my+8,this);
//
//			//RO画像の描画
//			NyARRasterImageIO.copy(ro, sink);
//			g.drawImage(sink,ins.left+320,ins.top+240,ins.left+32+320,ins.top+240+32,this);

		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    public void mainloop() throws Exception
    {
    	for(;;){
	    	//画像取得
	//    	this._src_image
	    	//処理
	    	this.update(this.getGraphics(),this._src_image);
	    	Thread.sleep(30);
    	}
    }

	public static void main(String[] args)
	{

		try {
			StaticImageDetectLabel mainwin = new StaticImageDetectLabel();
			mainwin.setVisible(true);
			mainwin.mainloop();
			// mainwin.startImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
}
}