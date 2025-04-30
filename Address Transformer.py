pip install requests tqdm
import json
import requests
import time
import os
from tqdm.notebook import tqdm  
import logging
from concurrent.futures import ThreadPoolExecutor

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class GoogleMapsGeocoder:
    
    def __init__(self, api_key, max_retries=3, delay=0.1):
        self.api_key = api_key
        self.max_retries = max_retries
        self.delay = delay
        self.base_url = "https://maps.googleapis.com/maps/api/geocode/json"
        self.requests_count = 0
    
    def geocode_address(self, address, context=None):

        # 如果提供了上下文，将其添加到地址中
        search_address = address
        if context:
            search_address = f"{address}, {context}"
            
        # 为USC建筑添加更多上下文以提高准确性
        if "University" not in search_address and "USC" not in search_address:
            if "Catalina Island" not in address and "San Diego" not in address and "Mountain View" not in address:
                search_address = f"{search_address}, University of Southern California, Los Angeles, CA"
        
        # 设置请求参数
        params = {
            "address": search_address,
            "key": self.api_key
        }
        
        # 尝试最多max_retries次
        for attempt in range(self.max_retries):
            try:
                # 发送请求
                response = requests.get(self.base_url, params=params, timeout=10)
                self.requests_count += 1
                
                # 解析响应
                data = response.json()
                
                # 检查响应状态
                if data["status"] == "OK":
                    
                    result = data["results"][0]
                    location = result["geometry"]["location"]
                    
                    return {
                        "lat": location["lat"],
                        "lng": location["lng"],
                        "formatted_address": result["formatted_address"],
                        "place_id": result.get("place_id", "")
                    }
                elif data["status"] == "ZERO_RESULTS":
                    logger.warning(f"找不到地址的坐标: {address}")
                    return None
                elif data["status"] == "OVER_QUERY_LIMIT":
                    logger.warning("超过API查询限制，延迟重试...")
                    time.sleep(2 ** attempt)  
                    continue
                else:
                    logger.error(f"地理编码错误: {data['status']} - {address}")
                    if "error_message" in data:
                        logger.error(f"错误信息: {data['error_message']}")
                    
                    
                    if data["status"] != "OVER_QUERY_LIMIT":
                        break
            
            except requests.exceptions.RequestException as e:
                logger.error(f"请求异常: {e} - {address}")
                time.sleep(1)  
            
           
            time.sleep(self.delay)
        
        return None

def process_building(geocoder, building, context=None):
    """处理单个建筑物，添加地理坐标"""
    address = building["address"]
    
    # 检查是否已有坐标数据
    if "lat" in building and "lng" in building:
        return building
    
    # 获取地址的坐标
    coords = geocoder.geocode_address(address, context)
    
    if coords:
        building["lat"] = coords["lat"]
        building["lng"] = coords["lng"]
        building["formatted_address"] = coords["formatted_address"]
        building["place_id"] = coords["place_id"]
    
    return building

def batch_process(geocoder, buildings, batch_size=10, context=None):
    """批量处理建筑物地址"""
    total = len(buildings)
    processed = 0
    
    with tqdm(total=total) as pbar:
        # 使用线程池进行并行处理
        with ThreadPoolExecutor(max_workers=min(5, batch_size)) as executor:
            for i in range(0, total, batch_size):
                batch = buildings[i:i+batch_size]
                
                # 提交所有任务到线程池
                futures = [
                    executor.submit(process_building, geocoder, building, context) 
                    for building in batch
                ]
                
                # 等待所有任务完成
                for future in futures:
                    future.result()
                
                processed += len(batch)
                pbar.update(len(batch))
                
                # 在批次之间添加延迟
                if i + batch_size < total:
                    time.sleep(1)
    
    return buildings

# 主要功能函数 
def geocode_usc_buildings(
    api_key, 
    input_file='usc_buildings.json', 
    output_file='usc_buildings_with_coordinates.json',
    batch_size=10,
    context='Los Angeles, CA'
):
    """主要函数，可直接在Jupyter/Colab中调用"""
    
    if not api_key:
        raise ValueError("未提供API密钥")
    
    try:
        # 读取JSON文件
        with open(input_file, "r", encoding="utf-8") as file:
            buildings = json.load(file)
    except FileNotFoundError:
        logger.error(f"找不到文件: {input_file}")
        raise
    except json.JSONDecodeError:
        logger.error(f"JSON解析错误: {input_file}")
        raise
    
    logger.info(f"开始处理 {len(buildings)} 个建筑物地址...")
    
    # 创建地理编码器
    geocoder = GoogleMapsGeocoder(api_key)
    
    # 批量处理建筑物
    buildings = batch_process(geocoder, buildings, batch_size, context)
    
    # 保存结果到新文件
    with open(output_file, "w", encoding="utf-8") as file:
        json.dump(buildings, file, ensure_ascii=False, indent=2)
    
    # 统计成功率
    success_count = sum(1 for b in buildings if "lat" in b and "lng" in b)
    logger.info(f"处理完成! {success_count}/{len(buildings)} 个地址成功添加了坐标。")
    logger.info(f"结果已保存到: {output_file}")
    logger.info(f"总API请求次数: {geocoder.requests_count}")
    
    return buildings

# 在Colab中使用示例:

# 设置您的API密钥
api_key = "AIzaSyDoaQhsf1v1jNeIsQL3ShpS7HiFRciihPI"  

# 调用函数
geocoded_buildings = geocode_usc_buildings(
    api_key=api_key,
    input_file='usc_buildings.json',
    batch_size=5  
)

# 查看结果
print(f"成功添加坐标的建筑物数量: {sum(1 for b in geocoded_buildings if 'lat' in b)}")


