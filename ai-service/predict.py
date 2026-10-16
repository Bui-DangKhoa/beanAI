# -*- coding: utf-8 -*-
"""
BeanAI - Script phân tích sâu bệnh cây đậu sử dụng Ultralytics YOLOv8/26 classification model (best.pt)
Cơ sở tri thức bệnh học và phác đồ điều trị chuyên sâu cây đậu chuẩn nông nghiệp Việt Nam.
"""

import sys
import json
import os
from pathlib import Path

# Cơ sở tri thức bệnh học và phác đồ điều trị chuyên sâu cho 11 lớp bệnh của model best.pt
DISEASE_KNOWLEDGE_BASE = {
    0: {
        "class_name": "Angular Leaf Spot",
        "vietnamese_name": "Bệnh đốm góc lá trên cây đậu",
        "scientific_name": "Phaeoisariopsis griseola (Pseudocercospora griseola)",
        "category": "Nấm bệnh hại lá (Fungal)",
        "severity": "Trung bình",
        "severity_code": "MEDIUM",
        "symptoms": "Vết bệnh ban đầu là các đốm màu nâu nhạt hoặc vàng nhạt, sau chuyển sang màu nâu xám sẫm. Vết bệnh bị chặn lại bởi các gân lá tạo thành hình đa giác góc cạnh rất rõ rệt. Khi độ ẩm cao, mặt dưới lá xuất hiện lớp phấn nấm màu xám tro, làm lá vàng úa và rụng sớm từ gốc lên ngọn.",
        "spread_conditions": "Phát triển mạnh ở nhiệt độ 20-25°C, độ ẩm không khí cao trên 85%, mưa dầm kéo dài hoặc ruộng đậu tưới phun mưa quá ẩm ướt đọng nước qua đêm.",
        "protocol": {
            "cultural_measures": "Cắt tỉa triệt để các lá già sát gốc bị đốm góc, thu gom cho vào bao kín đem tiêu hủy xa vườn. Giãn cách mật độ trồng tạo độ thông thoáng tán lá. Chuyển sang tưới rãnh hoặc tưới nhỏ giọt dưới gốc, tuyệt đối không tưới phun mưa tán lá vào chiều tối.",
            "biological_treatment": "Phun phòng trừ sinh học bằng chế phẩm vi khuẩn đối kháng Bacillus subtilis hoặc nấm Chaetomium cupreum. Kết hợp phun dịch chiết tỏi ớt kết hợp chất bám dính sinh học để ngăn chặn mầm nấm nảy mầm lây lan.",
            "chemical_treatment": "Khi tỷ lệ bệnh chớm xuất hiện trên 5%: Phun luân phiên Mancozeb 80WP (Dithane M-45, liều 50-60g/bình 16L), Chlorothalonil 75WP (Daconil 75WP, liều 30-40g/bình 16L) hoặc hoạt chất thế hệ mới Azoxystrobin + Difenoconazole (Amistar Top 325SC, liều 15ml/bình 16L). Phun 2 lần cách nhau 5-7 ngày. Thời gian cách ly (PHI): 7 ngày trước thu hoạch.",
            "recovery_nutrition": "Sau khi vết bệnh khô miệng xám lại, phun phân bón lá bổ sung Canxi - Bo và Silic để làm dầy vách tế bào biểu bì lá. Hòa Axit Humic tưới gốc kích thích rễ hút dinh dưỡng. Tạm thời ngừng bón phân đạm (Urê).",
            "prevention": "Xử lý hạt giống bằng nước ấm 54°C trong 25 phút hoặc xử lý men vi sinh trước khi gieo. Vệ sinh tàn dư mùa vụ và rắc vôi bột khử khuẩn đất 40-50kg/1.000m². Luân canh ít nhất 2 vụ với cây ngô, lúa nước hoặc rau họ hoa thập tự."
        },
        "treatment": "Cắt tỉa lá già bệnh tiêu hủy, ngừng tưới phun mưa. Phun luân phiên Mancozeb (Dithane M-45 80WP) hoặc Azoxystrobin + Difenoconazole (Amistar Top 325SC) 2 lần cách nhau 5-7 ngày. Bổ sung Canxi-Bo kích thích lá non hồi phục."
    },
    1: {
        "class_name": "Anthracnose",
        "vietnamese_name": "Bệnh thán thư trên cây đậu",
        "scientific_name": "Colletotrichum lindemuthianum",
        "category": "Nấm bệnh nguy hại cao (Fungal)",
        "severity": "Cao",
        "severity_code": "HIGH",
        "symptoms": "Vết bệnh ban đầu xuất hiện dọc theo gân lá mặt dưới có màu đỏ tím hoặc nâu sẫm lõm sâu xuống. Trên cuống lá và thân xuất hiện các vệt loét hình thoi màu nâu đen. Đặc biệt trên vỏ quả xuất hiện các đốm tròn trũng sâu màu nâu đen có viền gờ nổi cao; khi thời tiết ẩm ướt ứa khối bào tử nhầy màu hồng phấn, làm quả thối mục và hạt bị đen lép hoàn toàn.",
        "spread_conditions": "Nhiệt độ tối thích 17-24°C, độ ẩm không khí rất cao (>90%), mưa bão kéo dài kèm gió giật làm bào tử bắn tung tóe lây lan sang cả vườn đậu chỉ sau vài ngày.",
        "protocol": {
            "cultural_measures": "Khẩn trương cách ly vùng bệnh; tỉa bỏ toàn bộ quả và cành lá nhiễm bệnh thán thư đem chôn sâu có rắc vôi bột tiệt trùng. Ngưng hoàn toàn việc tưới phun nước lên tán lá. Không di chuyển làm việc trong luống đậu khi lá còn ướt.",
            "biological_treatment": "Ứng dụng chế phẩm nấm đối kháng Trichoderma asperellum tưới đẫm vùng gốc cây. Phun dung dịch nano bạc - nano đồng nồng độ 30-50ppm để diệt khuẩn và ức chế hệ sợi nấm Colletotrichum phát triển.",
            "chemical_treatment": "Phun dập dịch khẩn cấp bằng các loại thuốc đặc trị thán thư: Azoxystrobin 250g/l (Amistar 250SC, 15-20ml/bình 16L), Propiconazole + Difenoconazole (Tilt Super 300EC, 10-15ml/bình 16L) hoặc Prochloraz 450g/l. Phun ướt đều cả 2 mặt lá, thân và chùm quả. Phun lặp lại sau 5-7 ngày. Tuân thủ thời gian cách ly PHI 7-10 ngày.",
            "recovery_nutrition": "Tuyệt đối không bón phân đạm vô cơ hay phân gà tươi. Bón bổ sung phân Lân nung chảy và Kali Sunfat (K2SO4), phun phân bón lá giàu Magie và Kẽm vi lượng giúp quả non lành sẹo nhanh chóng.",
            "prevention": "Sử dụng 100% hạt giống F1 sạch bệnh đã qua xử lý khử nấm. Thu dọn và tiêu hủy toàn bộ xác thân cây đậu sau mùa thu hoạch. Xử lý đất trước khi gieo sạ bằng cách cày ải phơi đất và bón vôi tả."
        },
        "treatment": "Cắt bỏ ngay quả và cành lá bệnh đem chôn rắc vôi. Phun đặc trị bằng Azoxystrobin (Amistar 250SC) hoặc Tilt Super 300EC 2 lần cách nhau 5-7 ngày. Bón bổ sung Kali Sunfat và phân lân nung chảy để cây phục hồi vách tế bào."
    },
    2: {
        "class_name": "Cercospora Leaf Spot",
        "vietnamese_name": "Bệnh đốm lá Cercospora",
        "scientific_name": "Cercospora canescens / Cercospora cruenta",
        "category": "Nấm bệnh hại lá (Fungal)",
        "severity": "Trung bình",
        "severity_code": "MEDIUM",
        "symptoms": "Xuất hiện các đốm tròn hoặc bầu dục đường kính 2-6mm rải rác trên phiến lá. Tâm vết bệnh màu nâu xám hoặc xám trắng nhạt, có viền nâu đỏ sẫm rất sắc nét và bao quanh bởi một quầng vàng nhạt (dạng đốm mắt cua). Vết bệnh già dễ bị giòn vỡ thủng lỗ chỗ, làm lá mất diệp lục tố, suy giảm quang hợp nghiêm trọng và rụng lá hàng loạt.",
        "spread_conditions": "Phát sinh phổ biến trong mùa mưa ẩm, nhiệt độ từ 24-30°C, biên độ nhiệt ngày đêm cao có sương mù ẩm ướt vào sáng sớm.",
        "protocol": {
            "cultural_measures": "Vặt bỏ các lá bánh tẻ và lá già phía dưới gốc có mụn đốm bệnh đem tiêu hủy. Vun cao luống đậu để thoát nước gốc nhanh sau mưa, nhổ sạch cỏ dại quanh mép bờ mương.",
            "biological_treatment": "Sử dụng dầu neem hữu cơ ép lạnh (Neem oil nguyên chất) pha với tỷ lệ 5ml/lít nước kèm 2ml nước rửa chén sinh học khuấy đều phun đều mặt lá, hoặc chế phẩm nấm đối kháng Chaetomium định kỳ 7 ngày/lần.",
            "chemical_treatment": "Phun một trong các hoạt chất diệt nấm phổ rộng: Hexaconazole 5% (Anvil 5SC, liều 20-25ml/bình 16L), Carbendazim 500g/l (Carbenzim 500FL) hoặc Thiophanate-methyl 70WP (Topsin M). Phun ướt đều tán lá vào lúc sáng sớm ráo sương hoặc chiều mát. Cách ly 7 ngày.",
            "recovery_nutrition": "Bón phân cân đối NPK 15-15-15 kết hợp phun phân bón qua lá có chứa Magie và Kẽm (Zn) để thúc đẩy tạo lại diệp lục, giúp tán lá non dày và quang hợp khỏe mạnh.",
            "prevention": "Thực hiện luân canh cây trồng khác họ họ đậu (như ngô, lúa, khoai lang). Bón lót phân trùn quế hoặc phân chuồng đã ủ hoai mục bằng nấm Trichoderma."
        },
        "treatment": "Tỉa bỏ lá già có đốm mắt cua. Phun phòng trị bằng Hexaconazole (Anvil 5SC) hoặc Topsin M 70WP vào sáng sớm ráo sương. Bón phân NPK cân đối và phun phân bón lá bổ sung vi lượng Magie, Kẽm."
    },
    3: {
        "class_name": "Fresh Leaf",
        "vietnamese_name": "Lá đậu khỏe mạnh",
        "scientific_name": "Phaseolus vulgaris (Lá sinh trưởng bình thường)",
        "category": "Cây trồng phát triển khỏe mạnh (Healthy)",
        "severity": "An toàn",
        "severity_code": "SAFE",
        "symptoms": "Phiến lá màu xanh lục tươi sáng đều màu, bề mặt lá nhẵn bóng khỏe, mép lá nguyên vẹn tự nhiên không bị sâu cắn hay quăn quéo. Gân lá phát triển đối xứng rõ nét, không phát hiện vết hoại tử, mụn nấm hay bào tử vi khuẩn.",
        "spread_conditions": "Vườn đậu đang có điều kiện tiểu khí hậu lý tưởng: ánh sáng đầy đủ, độ ẩm đất 65-75%, dinh dưỡng cân đối và hệ sinh thái vi sinh vật có lợi hoạt động tốt.",
        "protocol": {
            "cultural_measures": "Duy trì chế độ tưới ẩm đều đặn vào buổi sáng sớm, tránh tưới đêm làm ướt sũng gốc. Thường xuyên tỉa bớt lá già sát gốc tạo độ thông thoáng gốc cây để đón ánh sáng.",
            "biological_treatment": "Định kỳ 10-14 ngày phun phòng ngừa sinh học bằng chế phẩm vi sinh hữu cơ EM hoặc nấm Trichoderma để tạo màng chắn bảo vệ tự nhiên trên bề mặt lá và vùng rễ cây.",
            "chemical_treatment": "Không cần sử dụng bất kỳ loại thuốc BVTV hóa học nào. Bảo tồn các loài thiên địch có ích trong vườn như bọ rùa, nhện bắt mồi và ong ký sinh.",
            "recovery_nutrition": "Bón thúc phân hữu cơ vi sinh, phân trùn quế định kỳ 15-20 ngày/lần. Khi cây bước vào giai đoạn trổ hoa - kết quả, phun bổ sung Canxi-Bo để tăng tỷ lệ đậu quả và hạn chế rụng quả non.",
            "prevention": "Duy trì vệ sinh cỏ dại quanh bờ ruộng, kiểm tra định kỳ 2 lần/tuần để phát hiện sớm các cá thể sâu hại hoặc bào tử bệnh di chuyển từ nơi khác đến."
        },
        "treatment": "Cây đậu đang sinh trưởng rất khỏe mạnh và đạt chuẩn! Tiếp tục duy trì tưới ẩm đều vào sáng sớm, bón phân hữu cơ vi sinh định kỳ và phun phòng ngừa nấm vi sinh Trichoderma mỗi 10-14 ngày."
    },
    4: {
        "class_name": "Fungi Pathogens",
        "vietnamese_name": "Nấm bệnh gây hại thân lá tổng hợp",
        "scientific_name": "Phoma exigua / Rhizoctonia solani / Fusarium spp.",
        "category": "Phức hợp nấm bệnh rễ thân lá (Fungal Complex)",
        "severity": "Trung bình",
        "severity_code": "MEDIUM",
        "symptoms": "Các mảng hoại tử màu nâu thẫm không có hình dạng nhất định xuất hiện ở mép lá hoặc dọc theo phiến lá, lá có vệt cháy ủng nước rồi khô giòn xơ xác. Phần gốc thân sát mặt đất có thể bị thắt eo hoặc lở cổ rễ màu nâu đen, làm tắc mạch dẫn khiến nhánh cây bị vàng héo rũ cục bộ.",
        "spread_conditions": "Đất trồng bị trũng ứ nước sau mưa bão, đất thịt nặng thoát nước kém, nhiệt độ 22-28°C kết hợp yếm khí rễ làm nấm đất bùng phát tấn công lên thân lá.",
        "protocol": {
            "cultural_measures": "Xới xáo nhẹ váng đất bề mặt để rễ thông thoáng và thoát ẩm nhanh. Cắt tỉa nhánh lá sát mặt đất đã bị thâm thối đem tiêu hủy ra ngoài ruộng. Tạm ngưng tưới nước 1-2 ngày cho khô ráo mặt luống.",
            "biological_treatment": "Hòa loãng chế phẩm nấm đối kháng Trichoderma virens kết hợp vi khuẩn Bacillus subtilis tưới đẫm quanh gốc cây và rãnh luống với liều 1kg pha 400 lít nước để ức chế nấm đất gây hại.",
            "chemical_treatment": "Tưới gốc và phun ướt tán lá bằng thuốc trị nấm phổ rộng: Metalaxyl-M 40g/kg + Mancozeb 640g/kg (Ridomil Gold 68WG, liều 50g/bình 16L), Fosetyl-aluminium (Aliette 800WG, liều 25-30g/bình 16L) hoặc Validamycin 5SL (trị nấm lở cổ rễ). Phun 2 lần cách nhau 5 ngày. Cách ly 7 ngày.",
            "recovery_nutrition": "Sau khi nấm bệnh ngừng lây lan, kích hoạt lại bộ rễ bằng phân bón rễ Axit Humic + Axit Fulvic hữu cơ, kết hợp phun phân bón lá chiết xuất từ rong biển để phục hồi quang hợp.",
            "prevention": "Lên luống cao 25-30cm, tạo rãnh thoát nước sâu giữa các luống. Rắc vôi bột khử trùng đất trước vụ gieo trồng 10-15 ngày. Không bón phân chuồng tươi chưa qua ủ hoai mục."
        },
        "treatment": "Xới nhẹ mặt luống thoát ẩm, cắt tỉa cành gốc bị thối. Phun và tưới gốc bằng Ridomil Gold 68WG hoặc Aliette 800WG 2 lần cách nhau 5 ngày. Tưới kích rễ bằng Axit Humic phục hồi bộ rễ non."
    },
    5: {
        "class_name": "Halo Blight",
        "vietnamese_name": "Bệnh đốm cháy vi khuẩn quầng vàng",
        "scientific_name": "Pseudomonas syringae pv. phaseolicola",
        "category": "Vi khuẩn gây bệnh nguy hại (Bacterial)",
        "severity": "Cao",
        "severity_code": "HIGH",
        "symptoms": "Vết bệnh ban đầu là những đốm nhỏ ngậm nước màu nâu xám trên lá, được bao bọc bởi một vùng quầng rộng màu vàng chanh sáng rất đặc trưng (quầng hào quang Halo Blight). Khi thời tiết ẩm ướt, bề mặt vết bệnh có giọt dịch vi khuẩn màu trắng sữa hoặc trắng đục rỉ ra; trên thân có vết loét dọc thâm đen, lá rụng sớm và cây đậu còi cọc nhanh chóng.",
        "spread_conditions": "Thời tiết mát ẩm nhiệt độ dưới 20-23°C, mưa nhiều, gió lốc làm rách mô lá tạo điều kiện cho vi khuẩn xâm nhập qua lỗ khí khổng và vết thương cơ giới.",
        "protocol": {
            "cultural_measures": "Khoanh vùng cách ly khu vực bệnh; nhổ bỏ cây bị bệnh nặng cho vào túi nilông đem tiêu hủy xa ruộng, rắc vôi bột vào gốc cây vừa nhổ. Tuyệt đối không lội qua lại vườn đậu khi lá còn ướt sương sáng.",
            "biological_treatment": "Phun các chế phẩm sinh học chứa vi khuẩn đối kháng Bacillus amyloliquefaciens hoặc dịch nano bạc kháng khuẩn nồng độ cao để ngăn chặn vi khuẩn phân chia tế bào.",
            "chemical_treatment": "Đây là bệnh do VI KHUẨN (không dùng thuốc trị nấm thông thường): Phun ngay các thuốc đặc trị vi khuẩn như Copper Oxychloride (Coc 85WP, 30g/bình 16L), Kasugamycin (Kasumin 2SL, 25-30ml/bình 16L), Bismerthiazol (Xanthomix 20WP) hoặc Oxolinic acid (Starner 20WP). Phun dập dịch 2 lần cách nhau 3-5 ngày. Cách ly 7 ngày.",
            "recovery_nutrition": "Không pha chung thuốc gốc đồng với phân bón lá chứa đạm. Khi bệnh ổn định, phun bổ sung phân bón lá có Silic và Kẽm để tăng độ bền vách tế bào chống vi khuẩn tái xâm nhập.",
            "prevention": "Sử dụng hạt giống được chứng nhận sạch bệnh vi khuẩn. Tuyệt đối không lấy hạt từ ruộng đậu đã từng bị nhiễm Halo Blight làm giống cho mùa sau."
        },
        "treatment": "Nhổ bỏ cây bệnh nặng rắc vôi gốc. Phun ngay thuốc đặc trị VI KHUẨN chuyên dụng như Kasugamycin (Kasumin 2SL), Starner 20WP hoặc thuốc gốc đồng Coc 85WP 2 lần cách nhau 4-5 ngày. Ngừng tưới phun mưa tán lá."
    },
    6: {
        "class_name": "Mosaic Virus",
        "vietnamese_name": "Bệnh virus khảm lá đậu",
        "scientific_name": "Bean Common Mosaic Virus (BCMV)",
        "category": "Virus thực vật nguy hại cao (Viral)",
        "severity": "Cao",
        "severity_code": "HIGH",
        "symptoms": "Mặt lá xuất hiện các mảng loang lổ xen kẽ giữa màu xanh lục sẫm và xanh vàng nhạt. Phiến lá bị phồng rộp biến dạng lồi lõm sần sùi, mép lá cong queo cụp xuống dưới. Đọt non bị chùn ngọn không vươn dài, cây còi cọc lùn hẳn so với bình thường, hoa bị rụng hàng loạt và quả cong queo hạt lép.",
        "spread_conditions": "Truyền qua hạt giống nhiễm virus và truyền cơ giới qua véc-tơ côn trùng chích hút (đặc biệt là rệp muội đen Aphis craccivora và bọ trĩ) trong điều kiện nắng ráo.",
        "protocol": {
            "cultural_measures": "Virus đã xâm nhập vào mạch cây thì không thể chữa khỏi: Cần khẩn trương nhổ bỏ tận gốc tất cả các cây có biểu hiện khảm sần sùi cho vào bao tải mang đi chôn sâu hoặc đốt bỏ để chặn nguồn lây truyền.",
            "biological_treatment": "Phun chế phẩm kích kháng thực vật Chitosan oligosaccharide hoặc dịch chiết rong biển để tăng cường hệ miễn dịch tự nhiên của các cây còn khỏe mạnh trong vườn. Treo bẫy dính màu vàng để bẫy rệp muội cánh.",
            "chemical_treatment": "Tập trung tiêu diệt triệt để nguồn côn trùng môi giới truyền bệnh (rệp muội, bọ trĩ): Phun luân phiên các hoạt chất Pymetrozine (Chess 50WG, 10g/bình 16L), Dinotefuran (Oshin 20WP, 10-15g/bình 16L), Imidacloprid (Confidor 200SL) hoặc dầu khoáng sinh học SK Enspray 99EC. Phun ướt đẫm ngọn non và mặt dưới lá. Cách ly 7-10 ngày.",
            "recovery_nutrition": "Đối với các cây còn lại trong vườn chưa có triệu chứng: Phun phân bón lá giàu Kẽm (Zn), Magie và Axit Amin để thúc đẩy quang hợp và hỗ trợ cây vượt qua áp lực dịch hại.",
            "prevention": "Sử dụng giống đậu F1 kháng virus BCMV; xử lý hạt giống sạch mầm bệnh. Giữ vườn luôn sạch cỏ dại xung quanh bờ ruộng vì cỏ dại là ký chủ lưu giữ virus."
        },
        "treatment": "Khẩn trương nhổ bỏ tận gốc cây khảm nặng đem đốt tiêu hủy. Phun dập dịch côn trùng môi giới (rệp muội, bọ trĩ) bằng Pymetrozine (Chess 50WG) hoặc Dinotefuran (Oshin 20WP). Phun Chitosan kích kháng cho các cây còn lại."
    },
    7: {
        "class_name": "Potassium Deficiency",
        "vietnamese_name": "Hiện tượng thiếu hụt dinh dưỡng Kali",
        "scientific_name": "Physiological Disorder: Potassium (K) Deficiency",
        "category": "Rối loạn dinh dưỡng sinh lý (Physiological)",
        "severity": "Nhẹ",
        "severity_code": "LOW",
        "symptoms": "Triệu chứng bắt đầu từ các lá già phía dưới: Mép rìa lá và chóp lá bị vàng úa sau đó khô cháy giòn từ rìa tiến dần vào trong phiến lá theo hình chữ V ngược, trong khi các gân lá chính vẫn duy trì màu xanh. Thân cây đậu mảnh mai yếu ớt, dễ bị đổ ngã khi gặp gió mưa, hoa ít đậu quả và hạt lép nhẹ cân.",
        "spread_conditions": "Không phải bệnh truyền nhiễm; xuất hiện do đất cát nghèo dinh dưỡng bị rửa trôi, đất chua phèn giữ kali kém, hoặc do nông dân bón quá nhiều phân đạm (Urê) gây ức chế hấp thu kali.",
        "protocol": {
            "cultural_measures": "Kiểm tra độ ẩm đất và điều chỉnh lượng nước tưới hợp lý để hòa tan phân bón. Tạm dừng ngay việc bón các loại phân đạm đơn hoặc phân hữu cơ tươi có hàm lượng đạm cao.",
            "biological_treatment": "Bón bổ sung phân trùn quế, tro trấu mục (nguồn kali hữu cơ tự nhiên rất giàu có) và tưới men vi sinh hòa tan khoáng Kali để phân giải kali khó tiêu trong đất thành dạng dễ hấp thu.",
            "chemical_treatment": "Cứu đói khẩn cấp bằng dinh dưỡng qua lá: Phun phân bón lá có hàm lượng Kali cao dạng Chelate như Kali Clorua hòa tan, Kali Sunfat (K2SO4 1-1.5%) hoặc KNO3 (liều 50g/bình 16L). Phun 2 lần cách nhau 4-5 ngày. Bón gốc bổ sung Kali Clorua (KCl) 4-5kg/1.000m² hòa nước tưới đều quanh gốc.",
            "recovery_nutrition": "Bón phân cân đối N-P-K theo công thức 12-12-18 hoặc 15-5-20 trong giai đoạn nuôi trái. Kết hợp bổ sung Canxi-Bo để vỏ quả đậu cứng cáp và hạt mẩy bóng.",
            "prevention": "Bón lót vôi nông nghiệp 40kg/sào để nâng pH đất lên mức 6.0 - 6.5 giúp rễ hấp thu kali tối đa. Tăng cường bón lót phân chuồng hoai mục giữ ẩm và giữ chất dinh dưỡng cho đất."
        },
        "treatment": "Hiện tượng rối loạn dinh dưỡng sinh lý, không lây lan. Cần phun phân bón lá giàu Kali (K2SO4 1% hoặc KNO3) 2 lần cách nhau 5 ngày. Bón gốc bổ sung phân Kali Clorua hòa tan quanh gốc và tạm ngừng bón đạm Urê."
    },
    8: {
        "class_name": "Rust",
        "vietnamese_name": "Bệnh gỉ sắt trên cây đậu",
        "scientific_name": "Uromyces appendiculatus",
        "category": "Nấm bệnh hại lá nguy hiểm (Fungal)",
        "severity": "Cao",
        "severity_code": "HIGH",
        "symptoms": "Xuất hiện hàng loạt mụn nhỏ nổi cộm màu vàng nhạt ở mặt trên lá, sau đó mặt dưới lá xuất hiện các ổ mụn bào tử màu nâu đỏ như bột gỉ sét kim loại. Khi dùng ngón tay miết nhẹ vào mặt dưới lá sẽ thấy bột màu nâu đỏ dính vào tay. Về cuối vụ mụn gỉ chuyển sang màu nâu đen, làm toàn bộ phiến lá vàng cháy xơ xác và rụng trơ cành non.",
        "spread_conditions": "Nhiệt độ ấm ẩm từ 21-27°C, có màng nước đọng trên mặt lá liên tục 6-8 giờ qua đêm hoặc do sương mù dày đặc vào sáng sớm.",
        "protocol": {
            "cultural_measures": "Vặt bỏ sớm các lá già có mụn gỉ sắt phía dưới gốc đem gom đốt tiêu hủy ngoài bìa ruộng. Tuyệt đối không tưới nước vào buổi chiều tối để tránh màng nước đọng trên mặt lá qua đêm.",
            "biological_treatment": "Sử dụng chế phẩm vi nấm đối kháng Verticillium lecanii hoặc nấm đối kháng Trichoderma phun bao vây ổ bệnh. Phun dịch chiết tinh dầu quế hoặc tỏi hữu cơ để ức chế nảy mầm của bào tử hạ gỉ sắt.",
            "chemical_treatment": "Phun thuốc đặc trị nấm gỉ sắt thế hệ Triazole: Hexaconazole 5% (Anvil 5SC, 20-25ml/bình 16L), Tebuconazole (Folicur 430SC, 10-15ml/bình 16L), Difenoconazole (Score 250EC) hoặc hỗn hợp Azoxystrobin + Difenoconazole (Amistar Top). Phun ướt đẫm mặt dưới lá. Phun lặp lại sau 5-7 ngày. Cách ly 7 ngày.",
            "recovery_nutrition": "Sau khi dập tắt các mụn gỉ sắt (mụn khô lại chuyển màu đen xám), phun phân bón lá bổ sung Kẽm (Zn) và Magie kết hợp Amino Acid giúp cây đâm chồi lá mới để tiếp tục quang hợp.",
            "prevention": "Dọn sạch tàn dư lá đậu rụng vụ trước đem tiêu hủy. Luân canh với cây lúa nước hoặc ngô. Sử dụng giống đậu có tính kháng nấm gỉ sắt cao."
        },
        "treatment": "Cắt tỉa lá có mụn gỉ sắt đem tiêu hủy, ngừng tưới nước tán lá vào ban đêm. Phun ngay thuốc đặc trị Hexaconazole (Anvil 5SC) hoặc Tebuconazole (Folicur 430SC) 2 lần cách nhau 6 ngày, phun đẫm mặt dưới lá."
    },
    9: {
        "class_name": "Tobacco Caterpillar",
        "vietnamese_name": "Sâu khoang ăn lá đậu",
        "scientific_name": "Spodoptera litura (Fabricius)",
        "category": "Côn trùng sâu hại ăn lá (Insect Pest)",
        "severity": "Cao",
        "severity_code": "HIGH",
        "symptoms": "Lá đậu bị sâu non cắn thủng lỗ chỗ hoặc khuyết mép lá từng mảng lớn, mật độ sâu cao có thể cắn trụi sạch phiến lá chỉ còn trơ lại gân chính xơ xác. Sâu non tuổi nhỏ màu xanh lục tụ tập cắn mặt dưới lá, sâu tuổi lớn chuyển màu nâu xám sẫm có hai hàng chấm đen hình tam giác hai bên sườn. Ban ngày sâu ẩn nấp dưới gốc hoặc mặt dưới lá, chiều tối và ban đêm bò lên cắn phá dữ dội.",
        "spread_conditions": "Thời tiết ấm áp, mùa khô hanh hoặc chuyển mùa, ngài sâu sinh sản nhanh tạo ra các ổ trứng hàng trăm con trên mặt lá bánh tẻ.",
        "protocol": {
            "cultural_measures": "Thường xuyên vạch mặt dưới lá kiểm tra và ngắt bỏ các ổ trứng sâu có phủ lớp lông tơ màu nâu vàng. Dùng tay bắt giết các ổ sâu non mới nở còn tụ tập thành đám. Đặt bẫy bả chua ngọt (mật mía + giấm + rượu + thuốc trừ sâu) vào ban đêm để diệt ngài bướm đực.",
            "biological_treatment": "Phun chế phẩm sinh học vi khuẩn Bacillus thuringiensis (Bt - Delfin WG, Dipel, liều 20-30g/bình 16L) hoặc chế phẩm nấm ký sinh côn trùng Beauveria bassiana (Nấm trắng) + Metarhizium anisopliae (Nấm xanh). Rất an toàn cho người và thiên địch, không lo kháng thuốc.",
            "chemical_treatment": "Khi mật độ sâu cao tuổi 2-3 cắn phá mạnh: Phun các hoạt chất thế hệ mới đặc trị sâu miệng nhai: Emamectin benzoate (Susupes 1.9EC, liều 10ml/bình 16L), Chlorantraniliprole (Prevathon 5SC, 10ml/bình 16L) hoặc Indoxacarb (Ammate 150SC). Phun vào buổi chiều mát (sau 16h30) khi sâu bắt đầu bò ra cắn lá. Cách ly 5-7 ngày.",
            "recovery_nutrition": "Sau khi diệt sạch sâu, tưới phân bón lá sinh học giàu Đạm hữu cơ (phân đạm cá thủy phân) và Axit Amin để kích thích đâm đọt non tái tạo tán lá quang hợp nhanh chóng.",
            "prevention": "Làm sạch cỏ dại quanh bờ ruộng, cày lật đất phơi ải diệt nhộng sâu khoang trong đất trước khi gieo trồng."
        },
        "treatment": "Ngắt bỏ ổ trứng và sâu non tụ tập. Phun chế phẩm sinh học Bt (Delfin WG) hoặc thuốc sinh học Emamectin benzoate (Susupes 1.9EC) / Prevathon 5SC vào chiều mát (sau 16h30) khi sâu bò ra ăn lá. Bổ sung đạm cá hồi phục chồi lá."
    },
    10: {
        "class_name": "Yellow Mosaic",
        "vietnamese_name": "Bệnh virus khảm vàng lá đậu",
        "scientific_name": "Mungbean Yellow Mosaic Virus (MYMV)",
        "category": "Virus thực vật nguy hại đặc biệt (Viral)",
        "severity": "Cao",
        "severity_code": "HIGH",
        "symptoms": "Lá xuất hiện các đốm màu vàng tươi rực rỡ xen lẫn các mảng xanh lục, sau đó các đốm vàng lan rộng nhanh chóng biến toàn bộ phiến lá thành màu vàng óng như lá úa, chỉ còn các đường gân chính giữ màu xanh. Cây đậu bị bệnh không phân cành, cây còi cọc lùn, hoa rụng hàng loạt và quả non bị lép hoàn toàn hoặc hạt bị dị dạng đen biến chất.",
        "spread_conditions": "Lây lan cực nhanh thông qua côn trùng môi giới là bọ phấn trắng (Bemisia tabaci) chích hút truyền virus từ cây bệnh sang cây khỏe trong điều kiện thời tiết nắng nóng khô hạn.",
        "protocol": {
            "cultural_measures": "Khẩn trương nhổ bỏ tận gốc toàn bộ cây bị triệu chứng khảm vàng rực rỡ đem chôn sâu có rắc vôi bột hoặc đốt tiêu hủy ngay. Dọn sạch cỏ dại quanh bờ ruộng vì cỏ dại là ký chủ dự trữ của virus và nơi trú ẩn của bọ phấn trắng.",
            "biological_treatment": "Treo bẫy dính màu vàng xung quanh và bên trong ruộng đậu (khoảng cách 5-7m một bẫy, treo ngang tầm tán lá đậu) vì bọ phấn trắng rất thích màu vàng. Phun dầu khoáng nông nghiệp hữu cơ SK Enspray 99EC nồng độ 0.4% để làm ngạt trứng và bọ phấn trưởng thành.",
            "chemical_treatment": "Tập trung tiêu diệt triệt để bọ phấn trắng truyền bệnh: Phun luân phiên Dinotefuran 20% (Oshin 20WP, liều 15g/bình 16L), Thiamethoxam 25% (Actara 25WG), Spirotetramat (Movento 150OD, liều 15ml/bình 16L) hoặc Pyriproxyfen (ức chế sinh sản bọ phấn). Phun 2 lần cách nhau 4-5 ngày vào sáng sớm khi bọ phấn ít bay lượn. Cách ly 7 ngày.",
            "recovery_nutrition": "Với các cây khỏe còn lại trong luống: Phun phân bón lá giàu Silic, Kali hữu cơ và Axit Humic để tăng cường sức chống chịu, hỗ trợ cây vượt qua stress dịch bệnh.",
            "prevention": "Sử dụng các giống đậu lai mới F1 có gen kháng virus MYMV. Bố trí lịch gieo trồng tập trung đồng loạt để tránh giai đoạn nhạy cảm của cây trùng với thời điểm bọ phấn trắng bùng phát."
        },
        "treatment": "Nhổ bỏ cây khảm vàng tiêu hủy ngay. Tiêu diệt triệt để bọ phấn trắng bằng bẫy dính màu vàng và phun luân phiên thuốc Dinotefuran (Oshin 20WP) hoặc Movento 150OD 2 lần cách nhau 5 ngày vào sáng sớm. Phun Silic và Kali hữu cơ tăng đề kháng."
    }
}

def predict(image_path, model_path):
    """
    Thực hiện phân tích hình ảnh lá cây họ đậu và suy luận bệnh học bằng mô hình YOLOv8-cls (best.pt).

    Tham số:
        image_path (str): Đường dẫn tuyệt đối hoặc tương đối tới tệp ảnh lá cây cần phân tích.
        model_path (str): Đường dẫn tới tệp trọng số mô hình PyTorch (.pt).

    Trả về:
        dict: Cấu trúc JSON gồm:
            - success (bool): Trạng thái thành công hay thất bại.
            - class_id (int): Mã số lớp bệnh được mô hình dự đoán xác suất cao nhất (Top-1).
            - class_name (str): Tên tiếng Anh của bệnh.
            - vietnamese_name (str): Tên tiếng Việt chính quy của bệnh hoặc tình trạng lá.
            - scientific_name (str): Tên khoa học của tác nhân gây bệnh (nấm, vi khuẩn, virus...).
            - category (str): Nhóm tác nhân gây hại (Nấm, Vi khuẩn, Virus, Côn trùng, Dinh dưỡng...).
            - confidence (float): Độ tin cậy dự đoán (phần trăm, ví dụ 98.5%).
            - severity (str): Mức độ nguy hiểm (Cao, Trung bình, An toàn).
            - severity_code (str): Mã mức độ (HIGH, MEDIUM, SAFE).
            - symptoms (str): Mô tả chi tiết triệu chứng nhận biết thực địa.
            - spread_conditions (str): Điều kiện môi trường, thời tiết thuận lợi cho bệnh lây lan.
            - treatment (str): Tóm tắt biện pháp xử lý nhanh.
            - treatment_protocol (dict): Phác đồ 5 bước chuẩn nông nghiệp (Canh tác, Sinh học, Hóa học, Phục hồi, Phòng ngừa).
            - top_candidates (list): Danh sách Top 5 ứng viên chẩn đoán liên quan kèm xác suất.
    """
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
        
        # Load mô hình YOLO Classification (best.pt)
        model = YOLO(model_path)
        
        # Chạy inference với ảnh đầu vào
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
        
        # Lấy thông tin bệnh học và phác đồ điều trị từ Knowledge Base
        disease_info = DISEASE_KNOWLEDGE_BASE.get(top1_index, {
            "class_name": str(result.names.get(top1_index, f"Class {top1_index}")),
            "vietnamese_name": str(result.names.get(top1_index, f"Bệnh cây đậu #{top1_index}")),
            "scientific_name": "Chưa xác định",
            "category": "Chưa phân loại",
            "severity": "Trung bình",
            "severity_code": "MEDIUM",
            "symptoms": "Triệu chứng bất thường trên phiến lá đậu.",
            "spread_conditions": "Đang theo dõi điều kiện thời tiết.",
            "treatment": "Tham khảo cán bộ khuyến nông địa phương để được tư vấn chính xác.",
            "protocol": {
                "cultural_measures": "Vệ sinh vùng lá bệnh và cách ly cây trồng.",
                "biological_treatment": "Phun nấm đối kháng Trichoderma hoặc chế phẩm vi sinh.",
                "chemical_treatment": "Sử dụng thuốc BVTV theo khuyến cáo cán bộ chuyên môn.",
                "recovery_nutrition": "Bón phân hữu cơ cân đối và bổ sung vi lượng.",
                "prevention": "Luân canh cây trồng và xử lý đất kỹ lưỡng."
            }
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

        protocol_data = disease_info.get("protocol", {})

        return {
            "success": True,
            "class_id": top1_index,
            "class_name": disease_info["class_name"],
            "vietnamese_name": disease_info["vietnamese_name"],
            "scientific_name": disease_info["scientific_name"],
            "category": disease_info.get("category", "Nấm bệnh"),
            "confidence": round(top1_conf * 100, 2),
            "severity": disease_info["severity"],
            "severity_code": disease_info["severity_code"],
            "symptoms": disease_info["symptoms"],
            "spread_conditions": disease_info.get("spread_conditions", ""),
            "treatment": disease_info["treatment"],
            "treatment_protocol": {
                "cultural_measures": protocol_data.get("cultural_measures", ""),
                "biological_treatment": protocol_data.get("biological_treatment", ""),
                "chemical_treatment": protocol_data.get("chemical_treatment", ""),
                "recovery_nutrition": protocol_data.get("recovery_nutrition", ""),
                "prevention": protocol_data.get("prevention", "")
            },
            "top_candidates": top_candidates
        }

    except Exception as e:
        return {
            "success": False,
            "error": f"Lỗi trong quá trình chạy mô hình AI: {str(e)}"
        }

def get_all_diseases():
    """Trả về danh mục toàn bộ 11 bệnh và phác đồ điều trị mẫu"""
    diseases = []
    for cid in sorted(DISEASE_KNOWLEDGE_BASE.keys()):
        item = dict(DISEASE_KNOWLEDGE_BASE[cid])
        item["class_id"] = cid
        diseases.append(item)
    return diseases

if __name__ == "__main__":
    if len(sys.argv) >= 2 and sys.argv[1] == "--list-all":
        print(json.dumps({"success": True, "diseases": get_all_diseases()}, ensure_ascii=False))
        sys.exit(0)

    if len(sys.argv) < 2:
        print(json.dumps({
            "success": False,
            "error": "Thiếu tham số đường dẫn ảnh. Sử dụng: python predict.py <image_path> [model_path] hoặc python predict.py --list-all"
        }, ensure_ascii=False))
        sys.exit(1)

    img_path = sys.argv[1]
    
    # Model path mặc định nếu không truyền
    if len(sys.argv) >= 3:
        mdl_path = sys.argv[2]
    else:
        base_dir = os.path.dirname(__file__)
        possible_paths = [
            os.environ.get("BEANAI_MODEL_PATH"),
            os.path.join(base_dir, "..", "src", "main", "resources", "static", "best.pt"),
            os.path.join(base_dir, "..", "target", "classes", "static", "best.pt"),
            os.path.join(base_dir, "..", "models", "best.pt"),
            os.path.join(base_dir, "best.pt")
        ]
        mdl_path = None
        for p in possible_paths:
            if p and os.path.exists(p):
                mdl_path = os.path.abspath(p)
                break
        if not mdl_path:
            mdl_path = os.path.abspath(os.path.join(base_dir, "..", "src", "main", "resources", "static", "best.pt"))

    output = predict(img_path, mdl_path)
    # In ra stdout dưới dạng JSON để Java Spring Boot đọc
    print(json.dumps(output, ensure_ascii=False))
