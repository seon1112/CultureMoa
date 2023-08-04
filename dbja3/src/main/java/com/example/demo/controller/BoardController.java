package com.example.demo.controller;

import java.util.ArrayList;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.example.demo.dao.BoardDAO_jpa;
import com.example.demo.dao.EventDAO_jpa;
import com.example.demo.entity.Board;
import com.example.demo.entity.Event;
import com.example.demo.entity.Member;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Setter;
import java.io.File;

import com.example.demo.dao.ReviewBoardDAO_jpa;
import com.example.demo.entity.Reviewboard;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@Setter
public class BoardController {
	//로그인 했을때만 이용가능 
	//로그인 했을때 아이디
	public String id="user02";
	
	public int pageSIZE = 25;
	public int totalRecord;
	public int totalPage;
	
	@Autowired
	private BoardDAO_jpa boarddao_jpa;
	@Autowired
	private EventDAO_jpa eventdao_jpa;
	@Autowired
	private ReviewBoardDAO_jpa reviewboarddao_jpa;
	//-----------------------후기 게시판
	//조회수 추가
	@GetMapping("/updateHit")
	@ResponseBody
	public void updateHit(int hit,int reviewno) {
		int re=reviewboarddao_jpa.updateHit(hit, reviewno);
	}
	//좋아요 개수 추가
	@GetMapping("/plusLike")
	@ResponseBody
	public void plusLike(int like,int reviewno) {
		int re=reviewboarddao_jpa.plusLike(like, reviewno);
	}
	
	//게시글 삭제
	@GetMapping("/deleteReview")
	@ResponseBody
	public void deleteReview(@RequestParam int reviewno) {
		int re=reviewboarddao_jpa.deleteByNo(reviewno);
	}
	
	//게시물 수정 페이지 이동
	@GetMapping("/boards/review/reviewUpdate")
	public void reviewupdate(@RequestParam int reviewno,Model model) {
		Reviewboard review=new Reviewboard();
		review=reviewboarddao_jpa.findByNo(reviewno);
		//후기 게시물 행사번호
		int eventno=review.getEventno();  
		//게시물 내용 가져오기
		model.addAttribute("r", review);
		//아이디
		model.addAttribute("id", id);
		//공연정보
		Event event=new Event();
		event= eventdao_jpa.findByEventno(eventno);
		model.addAttribute("event", event);
		
		Date today=new Date();
		model.addAttribute("today", today);
		//행사리스트
		model.addAttribute("list", eventdao_jpa.findAll());
	}
	//후기 게시글 수정
	@PostMapping("/boardUpdate")
	public ModelAndView boardUpdate(Reviewboard r,String Contents) {
		ModelAndView mav=new ModelAndView("redirect:/boards/review/reviewlist");
		//게시물 작성하기
		// Member 객체 생성 및 설정
		Member member = new Member();
		member.setId(id);
		// Reviewboard 객체에 Member 객체 설정
		r.setMember(member);
		r.setRegdate(new Date());
		r.setReviewcontent(Contents);
		reviewboarddao_jpa.save(r);
		return mav;
	}
	//게시물 상세 페이지 이동
	@GetMapping("/boards/review/reviewDetail")
	public String reviewDetail(@RequestParam int reviewno,Model model) {
		Reviewboard review=new Reviewboard();
		review=reviewboarddao_jpa.findByNo(reviewno);
		//후기 게시물 행사번호
		int eventno=review.getEventno();  
		//게시물 내용 가져오기
		model.addAttribute("r", review);
		//아이디
		model.addAttribute("id", id);
		//공연정보
		Event event=new Event();
		event= eventdao_jpa.findByEventno(eventno);
		model.addAttribute("event", event);
		
		
		String start=event.getEventstart().toString();
		String end=event.getEventend().toString();
		String day=start.substring(0, start.indexOf(" "))+" ~ "+end.substring(0, end.indexOf(" "));
		model.addAttribute("day", day);
		return "/boards/review/reviewDetail";
	}
	
	@GetMapping(value={"/boards/review/reviewlist", "/boards/review/reviewlist", "/boards/review/reviewlist/{page}", 
			"/boards/review/reviewlist/{keyword}/{page}", "/boards/review/reviewlist/{keyword}/{page}/{orderby}"})
	public ModelAndView reviewlist(HttpSession session, @PathVariable(required=false) String keyword, 
			@PathVariable(required=false) String orderby, HttpServletRequest request, @PathVariable(required = false) Integer page) {
		ModelAndView mav = new ModelAndView("/boards/review/reviewlist");

		if(orderby == null) {
			if(session.getAttribute("orderby") != null && !session.getAttribute("orderby").equals("")) {
				orderby = (String) session.getAttribute("orderby");
			}
			else {
				orderby="regdate";
			}	
		}
		String key = "all";
		if(page == null) {
			page = 1;
		}
		if(keyword == null) {
			key = "all";
		}

		Page<Reviewboard> list;
		
		if(session.getAttribute("keyword")!=null) {
			key = (String)session.getAttribute("keyword");
		}
		if(keyword != null) {
			key = keyword;
		}
		
		Pageable pageable;
		
		if(key.equals("all")) {
			if(orderby.equals("regdate")) {
				pageable = PageRequest.of(page-1, pageSIZE, Sort.by("regdate").descending());
			}
			else {
			    pageable = PageRequest.of(page-1, pageSIZE, Sort.by("reviewhit").descending());
			}
			list = reviewboarddao_jpa.findByBcategory("", pageable);
		}
		else {
			if(orderby.equals("regdate")) {
			    pageable = PageRequest.of(page-1, pageSIZE, Sort.by("regdate").descending());
			}
			else {
			    pageable = PageRequest.of(page-1, pageSIZE, Sort.by("reviewhit").descending());
			}
			list = reviewboarddao_jpa.findByBcategory(keyword, pageable);

		}
		
	    List<List<Reviewboard>> rows = new ArrayList<>();
	    List<Reviewboard> boardlist = new ArrayList<Reviewboard>();
	    List<Reviewboard> currentRow = null;
		session.setAttribute("keyword", key);
		session.setAttribute("orderby", orderby);
		
	    for (Reviewboard board : list.getContent()) {
	    	
	    	boardlist.add(board);
	        if (currentRow == null || currentRow.size() >= 4) {
	            currentRow = new ArrayList<>();
	            rows.add(currentRow);
	        }
	        currentRow.add(board);
	    }
		
		mav.addObject("list", boardlist);
		mav.addObject("currentPage", page);
		mav.addObject("totalPages", list.getTotalPages());

		return mav;
	}
	
	//후기 게시글 작성 페이지
	@GetMapping("/boards/review/insertBoard_review")
	public void reivew(Model model) {
		model.addAttribute("list", eventdao_jpa.findAll());
		model.addAttribute("id", id);
	}
	
	//후기 게시글 작성
	@PostMapping("/board")
	public ModelAndView board(Reviewboard r,String Contents) {
		ModelAndView mav=new ModelAndView("redirect:/boards/review/reviewlist");
		//게시물 작성하기
		r.setReviewno(reviewboarddao_jpa.findNextNo());	
		// Member 객체 생성 및 설정
		Member member = new Member();
		member.setId(id);
		// Reviewboard 객체에 Member 객체 설정
		r.setMember(member);
		r.setReviewlike(0);
		r.setRegdate(new Date());
		r.setReviewhit(1);
		r.setReviewcontent(Contents);
		reviewboarddao_jpa.save(r);
		return mav;
	}

	
	//후기 게시글 작성 시 사진이 있으면 폴더에 저장
	@PostMapping(value="/uploadSummernoteImageFile")
	@ResponseBody
	public String uploadSummernoteImageFile(@RequestParam("file") MultipartFile multipartFile) {
		JsonObject jsonObject = new JsonObject();
		
		String fileRoot = "C:\\summer\\";	//저장될 외부 파일 경로
		String originalFileName = multipartFile.getOriginalFilename();	//오리지날 파일명
		String extension = originalFileName.substring(originalFileName.lastIndexOf("."));	//파일 확장자
				
		String savedFileName = UUID.randomUUID() + extension;	//저장될 파일 명
		File targetFile = new File(fileRoot + savedFileName);	
		System.out.println(targetFile);
		HashMap<String, String> jsonResponse = new HashMap<>();
		
		try {
			InputStream fileStream = multipartFile.getInputStream();
			FileUtils.copyInputStreamToFile(fileStream, targetFile);	//파일 저장
			jsonResponse.put("url", "/summernoteImage/" + savedFileName);
			jsonResponse.put("responseCode", "success");

		} catch (IOException e) {
			FileUtils.deleteQuietly(targetFile);	//저장된 파일 삭제
			jsonResponse.put("responseCode", "error");
			e.printStackTrace();
		}
		 // Gson 객체를 사용하여 HashMap을 JSON 문자열로 변환
	    Gson gson = new Gson();
	    String jsonString = gson.toJson(jsonResponse);
	    
		
		return jsonString;
	}
	//-----------------------
	//-----------------------자유게시판
	//자유 게시글 수정
	@PostMapping("/freeUpdate")
	public ModelAndView freeUpdate(Board b,String Contents) {
		ModelAndView mav=new ModelAndView("redirect:/boards/board/freelist");
		//게시물 작성하기
		// Member 객체 생성 및 설정
		Member member = new Member();
		member.setId(id);
		b.setMember(member);
		b.setRegdate(new Date());
		b.setBoardcontent(Contents);
		b.setBcategory("자유");
		boarddao_jpa.save(b);
		return mav;
	}
	
	//게시물 수정 페이지 이동
	@GetMapping("/boards/board/updateFree")
	public void boardupdate(@RequestParam int boardno,Model model) {

		model.addAttribute("b", boarddao_jpa.findByNo(boardno));
		model.addAttribute("id", id);

		
	}
	//조회수 추가
	@GetMapping("/updatefreeHit")
	@ResponseBody
	public void updatefreHit(int hit,int boardno) {
		int re=boarddao_jpa.updateHit(hit, boardno);
	}
	
	//좋아요 개수 추가
	@GetMapping("/updatefreeLike")
	@ResponseBody
	public void updatefreeLike(int like,int boardno) {
		int re=boarddao_jpa.updatefreeLike(like, boardno);
	}
	
	//게시물 상세 페이지 이동
	@GetMapping("/boards/board/freeDetail")
	public String freeDetail(@RequestParam int boardno,Model model) {
		Board board=new Board();
		board=boarddao_jpa.findByNo(boardno);
		//게시물 내용 가져오기
		model.addAttribute("b", board);
		//아이디
		model.addAttribute("id", id);
		return "/boards/board/freeDetail";
	}
	//자유 게시글 작성 페이지
	@GetMapping("/boards/board/insertBoard_free")
	public void free(Model model) {
		model.addAttribute("id", id);
	}
	//게시글 저장
	@PostMapping("/free")
	public ModelAndView free(Board b,String Contents) {
		ModelAndView mav=new ModelAndView("redirect:/boards/board/freelist");
		b.setBoardno(boarddao_jpa.findNextNo());
		Member member = new Member();
		member.setId(id);
		b.setMember(member);
		b.setBoardlikes(0);
		b.setRegdate(new Date());
		b.setBoardhit(1);
		b.setBoardcontent(Contents);
		boarddao_jpa.save(b);
		return mav;
	}
	
	//게시글 list
	@GetMapping(value={"/boards/board/freelist", "/boards/board/freelist/", "/boards/board/freelist/{page}", 
			"/boards/board/freelist/{keyword}/{page}", "/boards/board/freelist/{keyword}/{page}/{orderby}"})
	public ModelAndView freelist(HttpSession session, @PathVariable(required=false) String keyword, 
			@PathVariable(required=false) String orderby, HttpServletRequest request, @PathVariable(required = false) Integer page) {
		ModelAndView mav = new ModelAndView("/boards/board/freelist");

		if(orderby == null) {
			if(session.getAttribute("orderby") != null && !session.getAttribute("orderby").equals("")) {
				orderby = (String) session.getAttribute("orderby");
			}
			else {
				orderby="regdate";
			}	
		}
		String key = "all";
		if(page == null) {
			page = 1;
		}
		if(keyword == null) {
			key = "all";
		}

		Page<Board> list;
		
		if(session.getAttribute("keyword")!=null) {
			key = (String)session.getAttribute("keyword");
		}
		if(keyword != null) {
			key = keyword;
		}
		
		Pageable pageable;
		
		if(key.equals("all")) {
			if(orderby.equals("regdate")) {
				pageable = PageRequest.of(page-1, pageSIZE, Sort.by("regdate").descending());
			}
			else {
			    pageable = PageRequest.of(page-1, pageSIZE, Sort.by("boardhit").descending());
			}
			list = boarddao_jpa.findByBcategory("자유", "", pageable);
		}
		else {
			if(orderby.equals("regdate")) {
			    pageable = PageRequest.of(page-1, pageSIZE, Sort.by("regdate").descending());
			}
			else {
			    pageable = PageRequest.of(page-1, pageSIZE, Sort.by("boardhit").descending());
			}
			list = boarddao_jpa.findByBcategory("자유", keyword, pageable);

		}
		
	    List<List<Board>> rows = new ArrayList<>();
	    List<Board> boardlist = new ArrayList<Board>();
	    List<Board> currentRow = null;
		session.setAttribute("keyword", key);
		session.setAttribute("orderby", orderby);
		
	    for (Board board : list.getContent()) {
	    	
	    	boardlist.add(board);
	        if (currentRow == null || currentRow.size() >= 4) {
	            currentRow = new ArrayList<>();
	            rows.add(currentRow);
	        }
	        currentRow.add(board);
	    }
	    
		mav.addObject("list", boardlist);
		mav.addObject("currentPage", page);
		mav.addObject("totalPages", list.getTotalPages());

		return mav;
	}
	
	
	@GetMapping("/boards/board/togetherlist")
	public void togetherlist() {
		
	}
	
}
