# -*- coding: utf-8 -*-
"""
BeanAI - Script phân tích sâu bệnh cây đậu sử dụng Ultralytics YOLOv8/26 classification model (best.pt)
"""

import sys
import json
import os
from pathlib import Path

# Định nghĩa cơ sở tri thức bệnh học cây đậu tiếng Việt cho 11 lớp bệnh của model
DISEASE_KNOWLEDGE_BASE = {
    0: {
        "class_name": "Angular Leaf Spot",
        "vietnamese_name": "Bệnh đốm góc lá trên cây đậu",
        "scientific_name": "Phaeoisariopsis griseola (Pseudocercospora)",
        "severity": "Trung bình",
        "severity_code": "MEDIUM",
        "symptoms": "Vết bệnh ban đầu là các đốm màu nâu nhạt, sau chuyển nâu sẫm và bị chặn bởi các gân lá tạo thành hình đa giác góc cạnh rõ rệt. Mặt dưới lá xuất hiện lớp phấn nấm màu xám tro.",
        "treatment": "Cắt tỉa lá già sát gốc bị bệnh đem tiêu hủy. Giữ khoảng cách trồng thông thoáng. Phun luân phiên các hoạt chất diệt nấm như Mancozeb, Chlorothalonil hoặc Copper Hydroxide khi bệnh mới chớm xuất hiện."
    },
    1: {
        "class_name": "Anthracnose",
        "vietnamese_name": "Bệnh thán thư trên cây đậu",
        "scientific_name": "Colletotrichum lindemuthianum",
        "severity": "Cao",
        "severity_code": "HIGH",
        "symptoms": "Vết bệnh màu nâu đen lõm sâu trên gân lá, cuống lá và vỏ quả. Khi trời ẩm ướt có khối bào tử màu hồng nhớt xuất hiện ở tâm vết bệnh, làm lá vàng rụng và quả thối hạt.",
        "treatment": "Dùng hạt giống sạch bệnh đã khử trùng. Thu gom tàn dư cây trồng sau thu hoạch. Phun đặc trị bằng các thuốc chứa hoạt chất Azoxystrobin, Difenoconazole hoặc Propiconazole (phun 2 lần cách nhau 7 ngày)."
    },
    2: {
        "class_name": "Cercospora Leaf Spot",
        "vietnamese_name": "Bệnh đốm lá Cercospora",
        "scientific_name": "Cercospora canescens / Cercospora cruenta",
        "severity": "Trung bình",
        "severity_code": "MEDIUM",
        "symptoms": "Các đốm tròn hoặc bầu dục màu nâu xám viền nâu đỏ, có quầng vàng nhạt bao quanh. Vết bệnh già có thể bị rách thủng ở giữa phiến lá, làm giảm mạnh khả năng quang hợp của cây.",
        "treatment": "Bón phân cân đối N-P-K, tránh bón thừa đạm. Phun phòng trừ bằng Carbendazim, Hexaconazole (Anvil), hoặc Thiophanate-methyl vào sáng sớm hoặc chiều mát."
    },
    3: {
        "class_name": "Fresh Leaf",
        "vietnamese_name": "Lá đậu khỏe mạnh",
        "scientific_name": "Phaseolus vulgaris (Lá bình thường)",
        "severity": "An toàn",
        "severity_code": "SAFE",
        "symptoms": "Phiến lá màu xanh tươi đều màu, gân lá phát triển tự nhiên, bề mặt lá bóng khỏe và không có bất kỳ dấu hiệu tổn thương do nấm khuẩn hay sâu chích hút.",
        "treatment": "Vườn đậu của bạn đang phát triển rất tốt! Hãy tiếp tục duy trì tưới ẩm đều đặn, tỉa bớt lá già dưới gốc và bón phân hữu cơ vi sinh định kỳ để tăng sức đề kháng cho cây."
    },
    4: {
        "class_name": "Fungi Pathogens",
        "vietnamese_name": "Nấm bệnh gây hại thân lá tổng hợp",
        "scientific_name": "Fungal Leaf Pathogens (Phoma / Rhizoctonia)",
        "severity": "Trung bình",
        "severity_code": "MEDIUM",
        "symptoms": "Bề mặt lá xuất hiện các vệt hoại tử loang lổ, mép lá bị úng nước rồi cháy khô xơ xác, xuất hiện các sợi tơ nấm hoặc mụn nấm nhỏ li ti trên mô lá bị chết.",
        "treatment": "Hạn chế tưới phun mưa làm ướt tán lá vào ban đêm. Sử dụng các loại thuốc diệt nấm phổ rộng như Metalaxyl, Fosetyl-aluminium hoặc Validamycin để bao vây vùng bệnh."
    },
    5: {
        "class_name": "Halo Blight",
        "vietnamese_name": "Bệnh đốm cháy vi khuẩn quầng vàng",
        "scientific_name": "Pseudomonas syringae pv. phaseolicola",
        "severity": "Cao",
        "severity_code": "HIGH",
        "symptoms": "Đốm nhỏ ngậm nước màu nâu được bao bọc bởi một vùng quầng rộng màu vàng sáng rất đặc trưng (quầng hào quang Halo). Bệnh lây lan rất nhanh trong điều kiện mát và mưa nhiều.",
        "treatment": "Đây là bệnh do vi khuẩn gây ra. Cần phun ngay thuốc gốc đồng như Đồng Oxyclorua (Coc 85) hoặc kháng sinh nông nghiệp chuyên trị vi khuẩn như Kasugamycin (Kasumin), Bismerthiazol."
    },
    6: {
        "class_name": "Mosaic Virus",
        "vietnamese_name": "Bệnh virus khảm lá đậu",
        "scientific_name": "Bean Common Mosaic Virus (BCMV)",
        "severity": "Cao",
        "severity_code": "HIGH",
        "symptoms": "Mặt lá loang lổ xen kẽ các mảng xanh đậm và xanh nhạt, phiến lá bị phồng rộp biến dạng, mép lá cong queo cụp xuống dưới, đọt non còi cọc ngừng phát triển.",
        "treatment": "Bệnh do virus truyền nhiễm qua hạt giống và côn trùng chích hút (rệp muội, bọ trĩ). Nhổ bỏ và tiêu hủy cây bị bệnh nặng. Phun thuốc diệt rệp muội triệt để bằng Pymetrozine hoặc Imidacloprid."
    },
    7: {
        "class_name": "Potassium Deficiency",
        "vietnamese_name": "Hiện tượng thiếu hụt dinh dưỡng Kali",
        "scientific_name": "Rối loạn sinh lý: Potassium (K) deficiency",
        "severity": "Nhẹ",
        "severity_code": "LOW",
        "symptoms": "Mép lá và đầu chóp lá bị vàng rồi cháy khô giòn mép lá từ rìa tiến dần vào trong phiến lá, các gân chính vẫn giữ màu xanh. Cây đậu yếu ớt, thân dễ đổ ngã và quả ít hạt.",
        "treatment": "Không phải bệnh truyền nhiễm. Cần bón bổ sung phân Kali Clorua (KCl) hoặc Kali Sunfat (K2SO4), phun phân bón vi lượng qua lá có hàm lượng Kali cao để cây hồi phục nhanh chóng."
    },
    8: {
        "class_name": "Rust",
        "vietnamese_name": "Bệnh gỉ sắt trên cây đậu",
        "scientific_name": "Uromyces appendiculatus",
        "severity": "Cao",
        "severity_code": "HIGH",
        "symptoms": "Xuất hiện hàng loạt mụn nhỏ nổi cộm màu nâu đỏ như rỉ sét kim loại ở cả hai mặt lá (chủ yếu là mặt dưới). Khi dùng tay miết thấy dính bột màu nâu đỏ. Lá khô cháy và rụng hàng loạt.",
        "treatment": "Tỉa bỏ triệt để các lá bị gỉ sắt gom đốt. Phun phòng trừ ngay bằng thuốc diệt nấm đặc trị: Hexaconazole (Anvil 5SC), Tebuconazole hoặc Difenoconazole khi vừa thấy đốm gỉ đầu tiên."
    },
    9: {
        "class_name": "Tobacco Caterpillar",
        "vietnamese_name": "Sâu khoang ăn lá đậu",
        "scientific_name": "Spodoptera litura (Fabricius)",
        "severity": "Cao",
        "severity_code": "HIGH",
        "symptoms": "Lá bị sâu cắn thủng lỗ chỗ hoặc khuyết mép lá lớn, chỉ còn trơ lại gân chính. Sâu non màu xanh lục hoặc nâu sẫm có các chấm đen hai bên sườn, ban ngày ẩn nấp dưới mặt lá hoặc gốc cây.",
        "treatment": "Bắt giết ổ sâu non mới nở. Sử dụng chế phẩm sinh học vi khuẩn Bt (Bacillus thuringiensis) hoặc phun thuốc sinh học Emamectin benzoate, Abamectin vào chiều mát khi sâu bò ra ăn."
    },
    10: {
        "class_name": "Yellow Mosaic",
        "vietnamese_name": "Bệnh virus khảm vàng lá đậu",
        "scientific_name": "Mungbean Yellow Mosaic Virus (MYMV)",
        "severity": "Cao",
        "severity_code": "HIGH",
        "symptoms": "Lá xuất hiện các đốm màu vàng tươi, sau đó lan rộng biến toàn bộ phiến lá thành màu vàng rực rỡ, chỉ còn gân lá màu xanh. Cây đậu không phân cành và hoa quả bị lép hoàn toàn.",
        "treatment": "Bệnh do bọ phấn trắng (Bemisia tabaci) truyền virus. Cần dùng bẫy dính màu vàng để bẫy bọ phấn, kết hợp phun thuốc diệt bọ phấn bằng Dinotefuran, Acetamiprid hoặc dầu khoáng sinh học SK Enspray."
    }
}

def predict(image_path, model_path):
    if not os.path.exists(image_path):
        return {
            "success": False,
            "error": f"Không tìm thấy file ảnh: {image_path}"
        }

    if not os.path.exists(model_path):
        return {
            "success": False,
            "error": f"Không tìm thấy file model: {model_path}"
        }

    try:
        from ultralytics import YOLO
        
        # Load mô hình YOLO Classification
        model = YOLO(model_path)
        
        # Chạy inference
        results = model.predict(source=image_path, imgsz=320, verbose=False)
        
        if not results or len(results) == 0:
            return {
                "success": False,
                "error": "Mô hình không trả về kết quả dự đoán."
            }

        result = results[0]
        
        # Lấy top prediction cho classification
        probs = result.probs
        top1_index = int(probs.top1)
        top1_conf = float(probs.top1conf.item())
        
        # Lấy top 5 classes
        top5_indices = [int(i) for i in probs.top5]
        top5_confs = [float(c.item()) for c in probs.top5conf]
        
        # Lấy thông tin bệnh học từ Knowledge Base
        disease_info = DISEASE_KNOWLEDGE_BASE.get(top1_index, {
            "class_name": str(result.names.get(top1_index, f"Class {top1_index}")),
            "vietnamese_name": str(result.names.get(top1_index, f"Bệnh cây đậu #{top1_index}")),
            "scientific_name": "Chưa xác định",
            "severity": "Trung bình",
            "severity_code": "MEDIUM",
            "symptoms": "Triệu chứng bất thường trên phiến lá đậu.",
            "treatment": "Tham khảo cán bộ khuyến nông địa phương để được tư vấn chính xác."
        })
        
        # Danh sách top candidates
        top_candidates = []
        for idx, conf in zip(top5_indices, top5_confs):
            c_info = DISEASE_KNOWLEDGE_BASE.get(idx, {})
            top_candidates.append({
                "class_id": idx,
                "class_name": c_info.get("class_name", str(result.names.get(idx, f"Class {idx}"))),
                "vietnamese_name": c_info.get("vietnamese_name", str(result.names.get(idx))),
                "confidence": round(conf * 100, 2)
            })

        return {
            "success": True,
            "class_id": top1_index,
            "class_name": disease_info["class_name"],
            "vietnamese_name": disease_info["vietnamese_name"],
            "scientific_name": disease_info["scientific_name"],
            "confidence": round(top1_conf * 100, 2),
            "severity": disease_info["severity"],
            "severity_code": disease_info["severity_code"],
            "symptoms": disease_info["symptoms"],
            "treatment": disease_info["treatment"],
            "top_candidates": top_candidates
        }

    except Exception as e:
        return {
            "success": False,
            "error": f"Lỗi trong quá trình chạy mô hình AI: {str(e)}"
        }

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(json.dumps({
            "success": False,
            "error": "Thiếu tham số đường dẫn ảnh. Sử dụng: python predict.py <image_path> [model_path]"
        }, ensure_ascii=False))
        sys.exit(1)

    img_path = sys.argv[1]
    
    # Model path mặc định nếu không truyền
    if len(sys.argv) >= 3:
        mdl_path = sys.argv[2]
    else:
        # Tự tìm best.pt
        possible_paths = [
            os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "static", "best.pt"),
            os.path.join(os.path.dirname(__file__), "..", "target", "classes", "static", "best.pt"),
            "D:/beanAI/src/main/resources/static/best.pt"
        ]
        mdl_path = None
        for p in possible_paths:
            if os.path.exists(p):
                mdl_path = os.path.abspath(p)
                break
        if not mdl_path:
            mdl_path = "D:/beanAI/src/main/resources/static/best.pt"

    output = predict(img_path, mdl_path)
    # In ra stdout dưới dạng JSON để Java Spring Boot đọc
    print(json.dumps(output, ensure_ascii=False))
