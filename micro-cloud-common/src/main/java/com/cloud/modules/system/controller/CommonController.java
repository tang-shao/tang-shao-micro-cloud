package com.cloud.modules.system.controller;

import com.cloud.common.api.Result;
import com.cloud.common.constant.CommonConstant;
import com.cloud.modules.utils.CommonUtils;
import com.cloud.modules.utils.oConvertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Slf4j
// @RestController
// @RequestMapping("/sys/common")
public class CommonController {

    // 文件上传根目录
    @Value(value = "${jeecg.path.upload}")
    private String uploadpath;

    /**
     * 本地：local minio：minio 阿里：alioss
     */
    @Value(value = "${jeecg.uploadType}")
    private String uploadType;

    /**
     * 文件统一上传
     */
    @PostMapping(value = "/upload")
    public Result<?> upload(HttpServletRequest request, HttpServletResponse response) {
        Result<Object> result = new Result<>();
        String savePath = "";
        String bizPath = "";
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        // 获取文件上传对象
        MultipartFile file = multipartRequest.getFile("file");
        if (CommonConstant.UPLOAD_TYPE_OSS.equals(uploadType)) {
            // 如果未指定目录,则使用阿里云默认指定目录
            bizPath = "upload";
            //result.setMessage("使用阿里云文件上传时，必须添加目录！");
            //result.setSuccess(false);
            //return result;
        }

        if (CommonConstant.UPLOAD_TYPE_LOCAL.equals(uploadType)) {
            // jeditor编辑器如何使 lcaol模式，采用 base64格式存储
            String jeditor = request.getParameter("jeditor");
            if (oConvertUtils.isNotEmpty(jeditor)) {
                result.setMessage(CommonConstant.UPLOAD_TYPE_LOCAL);
                result.setSuccess(true);
                return result;
            } else {
                // 本地上传
                savePath = this.uploadLocal(file,bizPath);
            }
        } else {
            // oss 上传
            // savePath = CommonUtils.upload(file,bizPath,uploadType);
        }
        if (oConvertUtils.isNotEmpty(savePath)) {
            result.setMessage(savePath);
            result.setSuccess(true);
        } else {
            result.setMessage("上传失败！");
            result.setSuccess(false);

        }
        return result;
    }

    /**
     * 本地文件上传
     */
    private String uploadLocal (MultipartFile mf, String bizPath){
        try {
            String ctxPath = uploadpath;
            String fileName = null;
            File file = new File(ctxPath + File.separator + bizPath + File.separator );
            if(!file.exists()){
                // 如果文件不存在就创建
                file.mkdirs();
            }
            // 获取到文件原始名称
            String orgName = mf.getOriginalFilename();
            orgName = CommonUtils.getFileName(orgName);
            if(orgName.indexOf(".")!=-1){
                fileName = orgName.substring(0, orgName.lastIndexOf(".")) + "_" + System.currentTimeMillis() + orgName.substring(orgName.indexOf("."));
            }else{
                fileName = orgName+ "_" + System.currentTimeMillis();
            }
            // 获取上传路径
            String savePath = file.getPath() + File.separator + fileName;
            File savefile = new File(savePath);
            FileCopyUtils.copy(mf.getBytes(), savefile);
            String dbpath = null;
            if(oConvertUtils.isNotEmpty(bizPath)){
                dbpath = bizPath + File.separator + fileName;
            }else{
                dbpath = fileName;
            }
            if (dbpath.contains("\\")) {
                dbpath = dbpath.replace("\\", "/");
            }
            return dbpath;

        }catch (IOException e){
            log.error(e.getMessage(),e);
        }
        return "";
    }


    /**
     * 图片预览  & 文件下载
     */
    @GetMapping("/static/**")
    public void view(HttpServletRequest request,HttpServletResponse response){
        // ISO-8859-1 ==> UTF-8 进行编码转换
        String imgPath = extractPathFromPattern(request);
        if(oConvertUtils.isEmpty(imgPath) || imgPath=="null"){
            return;
        }
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            imgPath = imgPath.replace("..", "");
            if(imgPath.endsWith(",")){
                imgPath = imgPath.substring(0, imgPath.length() - 1);
            }
            // 拼接文件路径
            String filePath = uploadpath + File.separator + imgPath;
            File file = new File(filePath);
            if(!file.exists()){
                response.setStatus(404);
                throw new RuntimeException("文件不存在..");
            }
            response.setContentType("application/force-download");// 设置强制下载不打开
            response.addHeader("Content-Disposition",
                    "attachment;fileName=" + new String(file.getName().
                            getBytes("UTF-8"),"iso-8859-1"));
            inputStream = new BufferedInputStream(new FileInputStream(filePath));
            outputStream = response.getOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0 ){
                outputStream.write(buf,0,len);
            }
            // 把内容强制写到浏览器中
            response.flushBuffer();
        }catch (IOException e){
            log.error("预览文件失败" + e.getMessage());
            response.setStatus(404);
            e.printStackTrace();
        }finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }


    }


    private static String extractPathFromPattern(final HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, path);
    }
}
