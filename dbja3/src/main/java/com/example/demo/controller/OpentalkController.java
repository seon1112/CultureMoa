package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.dao.opentalkDAO_jpa;
import com.example.demo.dao.opentalkDAO_mb;
import com.example.demo.entity.Member;
import com.example.demo.entity.Opentalk;
import com.example.demo.vo.OpentalkVO;

import jakarta.servlet.http.HttpSession;

@Controller
public class OpentalkController {

	private int nowNo; //현재 마지막 채팅 번호
	
	@Autowired
	private opentalkDAO_jpa opentalkdao_jpa;
	@Autowired
	private opentalkDAO_mb opentalkdao_mb;
	
	//------------채팅창 열었을 때
	@GetMapping("/opentalk")
	public void talk(Model model, HttpSession session) {
		if(session.getAttribute("m") != null) {
			Member m = (Member)session.getAttribute("m");	
			model.addAttribute("id", m.getId());
		}
		else {
			model.addAttribute("id", null);
		}
		int now=opentalkdao_jpa.nextNo()-1;
		nowNo=now;
		List<OpentalkVO> list=null;
		list=opentalkdao_mb.findAllTalk();
		model.addAttribute("talk", list);
		model.addAttribute("now", nowNo);

	}
	
	//------------채팅창 업데이트
	@GetMapping("updateTalk")
	@ResponseBody
	public List<OpentalkVO> updateTalk(int now){
		nowNo=opentalkdao_jpa.nextNo()-1;  //nowNo를 db에 있는 마지막 채팅 번호로 업데이트
		List<OpentalkVO> list=null;
		if(now!=nowNo) {  //채팅창에 있는 마지막 no와 db에 있는 마지막 no가 다를때 업데이트
			int start=now+1;
			int end=nowNo;
			HashMap<String, Integer> map=new HashMap<>();
			map.put("start", start);
			map.put("end", end);
			list=opentalkdao_mb.findByNo(map);	
		}
		
		return list;
	}
	
	//------------채팅을 입력했을 때
	@GetMapping("insertTalk")
	@ResponseBody
	public void insertTalk(String talk, String id) {
		int next=opentalkdao_jpa.nextNo();
		nowNo=next;  //현재 마지막 채팅 번호 업데이트
		Opentalk o=new Opentalk();
		o.setTalkcontent(talk);
		o.setTalkno(next);
		o.setMemberId(id); 
		opentalkdao_jpa.insert(o);
		
	}
}
