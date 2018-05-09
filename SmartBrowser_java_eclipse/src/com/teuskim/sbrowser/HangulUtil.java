package com.teuskim.sbrowser;


public class HangulUtil {
	
	private static final int JASO_START_INDEX = 0x3131;
	private static final int HANGUL_START_INDEX = 0xAC00;
	private static final int JAUM_FULL_SIZE = 30;  // 곁자음 포함한 수
	
	// 곁자음/곁모음 포함한 총 51개의 자소 중 키보드에 들어간 33개의 자소에 대한 인덱스
	public static final int[] JASO_ARR = {0,1,3,6,7,8,16,17,18,20,21,22,23,24,25,26,27,28,29
									  //  ㄱㄲ ㄴㄷㄸ ㄹ ㅁ ㅂ  ㅃ ㅅ ㅆ  ㅇ ㅈ  ㅉ ㅊ  ㅋ ㅌ ㅍ  ㅎ
											,30,31,32,33,34,35,36,37,38,42,43,47,48,50
									    //   ㅏ  ㅐ ㅑ  ㅒ ㅓ ㅔ  ㅕ ㅖ ㅗ  ㅛ  ㅜ ㅠ ㅡ  ㅣ
											,39,40,41,44,45,46,49};
										//   ㅘ ㅙ  ㅚ ㅝ  ㅞ ㅟ  ㅢ

	// 중성에 들어갈 수 있는 모음들의 인덱스
	public static int[] JUNGSUNG_ARR = {0,1,2,3,4,5,6,7,8,12,13,17,18,20,9,10,11,14,15,16,19};
									//  ㅏㅐ ㅑㅒㅓ ㅔㅕ ㅖㅗ ㅛ  ㅜ ㅠ  ㅡ ㅣ ㅘ ㅙ ㅚ  ㅝ ㅞ  ㅟ ㅢ
	
	// 종성에 들어갈 수 있는 자음들의 인덱스. ㄱ이 1이다. -1인 경우 종성에 들어갈 수 없으니 다음 음절 초성으로 분리한다.
	public static int[] JONGSUNG_ARR = {1,2,4,7,-1,8,16,17,-1,19,20,21,22,-1,23,24,25,26,27};
									//  ㄱㄲ ㄴㄷ ㄸ ㄹ ㅁ ㅂ  ㅃ ㅅ ㅆ  ㅇ ㅈ  ㅉ ㅊ  ㅋ ㅌ  ㅍ ㅎ
			
	public static String separateJaso(String word){
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<word.length(); i++){
			char ch = word.charAt(i);
			if((int)ch >= HANGUL_START_INDEX){  // 조합형 한글이면 분리하여 반환한다.
				int index = (int)ch - HANGUL_START_INDEX;
				int chosungIndex = index / (21*28);
				int jungsungIndex = (index % (21*28)) / 28;
				int jongsungIndex = index % 28;
				
				sb.append((char)(JASO_START_INDEX+JASO_ARR[chosungIndex]));
				sb.append((char)(JASO_START_INDEX+JAUM_FULL_SIZE+jungsungIndex));
				if(jongsungIndex > 0)
					sb.append((char)(JASO_START_INDEX+JASO_ARR[jongToCho(jongsungIndex)]));
			}
			else
				sb.append(ch);
		}
		return sb.toString();
	}
	
	private static int jongToCho(int jong){
		int choIdx = 0;
		for(int i=0; i<JONGSUNG_ARR.length; i++){
			if(jong == JONGSUNG_ARR[i]){
				choIdx = i;
				break;
			}
		}
		return choIdx;
	}
}
