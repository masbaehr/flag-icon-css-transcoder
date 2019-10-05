package flag.icon.css.transcoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.io.IOUtils;

/**
 * Converts SVG flags to PNG/JPEG and BASE64 (usable via <img src> and background CSS)
 * This will reduce the total flag size, as some flags are very big as SVG (All flags as SVG: 2.34MB, as PNG: 196KB, as JPG: 294KB)
 * Thanks to https://github.com/lipis/flag-icon-css for doing the work of maintaining all flags :-) 
 */
public class FlagIconTranscoder {

	//Edit this array if you don't need certain flags
	public static String[] supportedIsos = {"ad","ae","af","ag","ai","al","am","ao","aq","ar","as","at","au","aw","ax","az","ba","bb","bd","be","bf","bg","bh","bi","bj","bl","bm","bn","bo","bq","br","bs","bt","bv",
	"bw","by","bz","ca","cc","cd","cf","cg","ch","ci","ck","cl","cm","cn","co","cr","cu","cv","cw","cx","cy","cz","de","dj","dk","dm","do","dz","ec","ee","eg","eh","er"/*,"es-ca"*/,"es","et","eu","fi","fj","fk","fm",
	"fo","fr","ga",/*"gb-eng","gb-nir","gb-sct","gb-wls",*/"gb","gd","ge","gf","gg","gh","gi","gl","gm","gn","gp","gq","gr","gs","gt","gu","gw","gy","hk","hm","hn","hr","ht","hu","id","ie","il","im","in","io","iq",
	"ir","is","it","je","jm","jo","jp","ke","kg","kh","ki","km","kn","kp","kr","kw","ky","kz","la","lb","lc","li","lk","lr","ls","lt","lu","lv","ly","ma","mc","md","me","mf","mg","mh","mk","ml","mm","mn","mo",
	"mp","mq","mr","ms","mt","mu","mv","mw","mx","my","mz","na","nc","ne","nf","ng","ni","nl","no","np","nr","nu","nz","om","pa","pe","pf","pg","ph","pk","pl","pm","pn","pr","ps","pt","pw","py","qa","re","ro",
	"rs","ru","rw","sa","sb","sc","sd","se","sg","sh","si","sj","sk","sl","sm","sn","so","sr","ss","st","sv","sx","sy","sz","tc","td","tf","tg","th","tj","tk","tl","tm","tn","to","tr","tt","tv","tw","tz","ua",
	"ug","um","un","us","uy","uz","va","vc","ve","vg","vi","vn","vu","wf","ws","xk","ye","yt","za","zm","zw"};

	//Use this link if you want 1x1 aspect-ratio sized flags: https://github.com/lipis/flag-icon-css/tree/master/flags/1x1/{iso}.svg";
	public static String flagGithubPath = "https://raw.githubusercontent.com/lipis/flag-icon-css/master/flags/4x3/{iso}.svg";
	
	public static void main(String[] args) throws Exception {
		
		String current = new java.io.File( "." ).getCanonicalPath();

		StringBuilder pngJsArray = new StringBuilder();
		StringBuilder jpgJsArray = new StringBuilder();

		pngJsArray.append("const flagDataForIso = {");
		jpgJsArray.append("const flagDataForIso = {");

		for(String iso : supportedIsos){
			System.out.println("Processing iso: " + iso);
			System.out.println("Build download url: " + flagGithubPath.replace("{iso}", iso));
			Path svgfilePath = Paths.get(current+"\\generated\\svg\\"+iso+".svg");
			Files.createDirectories(svgfilePath.getParent());

			//DOWNLOAD LATEST SVG FILES - DELETE THE generated\svg folder to download them again
			if(!Files.exists(svgfilePath)){
				System.out.println("Writing svg for iso: "+iso);
				String svgString = IOUtils.toString(new URL(flagGithubPath.replace("{iso}", iso)).openStream(), "UTF-8");
				Files.write(svgfilePath, svgString.getBytes());
			} else {
				System.out.println("Skipping iso: "+iso);
			}

			//CONVERT EACH FILE TO PNG
			String svgString = IOUtils.toString(new FileInputStream(new File(current+"\\generated\\svg\\"+iso+".svg")), "UTF-8");
			//FIX: The attribute "xlink:href" of the element <use> is required
			if(iso.equals("bo")){
				svgString = svgString.replaceAll("<use.*", "");
			}
			if(iso.equals("nc")){
				svgString = svgString.replaceAll("<path d", "<path id=\"leaf\" d");
			}
			
			//write pngs
			byte[] pngFile = transcodeSVG(svgString, "PNG");
			Files.write(Paths.get(current+"\\generated\\png\\"+iso+".png"), pngFile);
			//write jpegs
			byte[] jpgFile = transcodeSVG(svgString, "JPG");
			Files.write(Paths.get(current+"\\generated\\jpg\\"+iso+".jpg"), jpgFile);
			//write js array base64

			//"data:image/jpeg;base64,"
			 

			String base64Png = Base64.getEncoder().encodeToString(pngFile);
			pngJsArray.append("\t\""+iso + "\":\"" + base64Png+"\",\n");

			String base64Jpg = Base64.getEncoder().encodeToString(jpgFile);
			jpgJsArray.append("\t\""+iso + "\":\"" + base64Jpg+"\"\n");

		}

		pngJsArray.append("};");
		jpgJsArray.append("};");

		Files.createDirectories(Paths.get(current+"\\generated\\js\\"));
		//data:image/png;base64,   was omitted to save some bytes
		Files.write(Paths.get(current+"\\generated\\js\\flags_png.js"), pngJsArray.toString().getBytes());
		//data:image/jpeg;base64,  was omitted to save some bytes
		Files.write(Paths.get(current+"\\generated\\js\\flags_jpg.js"), jpgJsArray.toString().getBytes());
	 
	}
	
 
 
	 
	
	public static byte[] transcodeSVG(String svgString, String fileExt) throws Exception {
		// Create a JPEG transcoder
		ImageTranscoder t = null;
		if(fileExt.equals("PNG")){
			t = new PNGTranscoder();
		}
		if(fileExt.equals("JPG")){
			t = new JPEGTranscoder();
			t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, Float.valueOf(0.80f));
		}
        // Set the transcoding hints.
        t.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT, Float.valueOf(20));
        t.addTranscodingHint(JPEGTranscoder.KEY_WIDTH, Float.valueOf(27));
		// Create the transcoder input.        
		InputStream instr = new ByteArrayInputStream(svgString.getBytes(StandardCharsets.UTF_8));

		
        TranscoderInput input = new TranscoderInput(instr);
        // Create the transcoder output.
       
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(ostream);
        // Save the image.
		t.transcode(input, output);
		
		return ostream.toByteArray();
	}

 

}
