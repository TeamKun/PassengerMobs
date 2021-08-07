# PassengerMobs
Mobがスポーンする時、周囲に同種のMobがいたら、そのMobに重なるプラグイン。

## 動作環境
- Minecraft 1.16.5
- PaperMC 1.16.5

## コマンド
- PassengerMobs (pm)
    - radius [number] Mobが重なりうる範囲を指定する。0以上。デフォルトは20。
  
## 仕様
- プラグイン有効化時もMobを重ねる動作を実行。
  - まだどのMobにも重なっていないMobが、他のMobに重なりうる対象となる。
  - 有効化時点でMobがより多く重なっているMobに対して、優先してMobを重ねる。